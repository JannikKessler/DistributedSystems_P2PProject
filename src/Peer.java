import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings({"AccessStaticViaInstance", "SynchronizeOnNonFinalField"})
public class Peer {

    // Geteilte Attribute
    private PeerObject entryServer;
    private ArrayList<PeerObject> peerList;
    private ArrayList<SearchObject> searchList;
    private int searchIDCounter = 0;
    private Gui gui;
    private Thread keepAlive;
    private boolean isServer = false;
    private PeerObject myPeer;
    private Point location;
    private ServerSocket myServer;
    private PeerUtilities peerUtilities;

    // Server Attribute
    private int idCounter = 0;
    private Thread cleanPeerList;

    // FINALs
    public static final int INSERT = 1;
    public static final int REMOVE = 2;

    public Peer() {
        init(-1, null);
    }

    public Peer(int port) {
        init(port, null);
    }

    public Peer(int port, Point location) {
        init(port, location);
    }

    private void init(int port, Point location) {
        peerUtilities = new PeerUtilities(this);
        peerList = new ArrayList<>();
        searchList = new ArrayList<>();
        myPeer = new PeerObject(peerUtilities.getMyIpAsByteArray(),
            peerUtilities.charToByteArray((char) ((port == -1) ? peerUtilities.getStandardPort() : port)), new byte[2]);
        isServer = isServer();
        this.location = location;
    }

    public void startPeer() {

        try {

            peerUtilities.printMyIp();

            if (Utilities.isShowGui()) {
                gui = new Gui(isServer, location, this);
                AnzeigeThread at = new AnzeigeThread(this);
                at.start();
            }

            if (!isServer) {

                entryServer = new PeerObject(peerUtilities.getServerIpAsByteArray(), peerUtilities.getStandardPortAsByteArray(),
                    new byte[2]);

                peerUtilities.printLogInformation("Client gestartet");

                byte[] entryMsg = createEntryMsg();
                while (!sendMsg(entryServer, entryMsg)) {
                    System.err.println("EntryServer konnte nicht erreicht werden\nVersuche es erneut...");
                    Thread.sleep(1000);
                }

                byte[] entyResponeMessage = entryServer.getInFromPeerStream().readNBytes(36);
                processEntryResponeMessage(entryServer, entyResponeMessage);
                entryServer.closeStreams();

            } else {
                peerUtilities.printLogInformation("Server gestartet");

                cleanPeerList = new Thread(() -> {
                    try {
                        while (true) {

                            long grenzwert = new Date().getTime()
                                - Variables.getIntValue("time_server_max_without_keep_alive");
                            ArrayList<PeerObject> deleteList = new ArrayList<>();
                            for (PeerObject p : getPeerList()) {
                                if (p.getTimestamp().getTime() < grenzwert)
                                    deleteList.add(p);
                            }
                            deletePeersFromPeerList(deleteList);
                            Thread.sleep(Variables.getIntValue("time_serverlist_clean"));
                        }
                    } catch (Exception e) {
                        peerUtilities.errorMessage(e);
                    }
                });
                cleanPeerList.start();
            }

            keepAlive = new Thread(() -> {
                while (true) {
                    try {
                        if (isServer) {
                            addPeer(myPeer);
                        } else {
                            byte[] msg = createIAmAliveMsg();
                            sendMsg(entryServer, msg);
                            entryServer.closeStreams();
                        }
                        Thread.sleep(Variables.getIntValue("time_send_keep_alive"));
                    } catch (Exception e) {
                        peerUtilities.errorMessage(e);
                    }
                }
            });
            keepAlive.start();

            if (peerUtilities.isShowGui())
                gui.setHeadline((isServer ? "Server" : "Peer"), myPeer.getIpAsString(), myPeer.getPortAsInt(), myPeer.getIdAsInt());

            myServer = new ServerSocket(myPeer.getPortAsInt());

            while (!myServer.isClosed()) {

                Socket connectionSocket = myServer.accept();

                Thread mainThread = new Thread(() -> {

                    try {

                        InputStream inFromPeer = connectionSocket.getInputStream();
                        OutputStream outToPeer = connectionSocket.getOutputStream();

                        byte[] msgTag = inFromPeer.readNBytes(1);
                        int tag = msgTag[0];

                        byte[] msgVersion = inFromPeer.readNBytes(1);
                        int version = msgVersion[0];

                        byte[] msgPeerFrom = new byte[8];
                        inFromPeer.readNBytes(msgPeerFrom, 0, 6);

                        if (tag != 1)
                            inFromPeer.readNBytes(msgPeerFrom, 6, 2);

                        PeerObject peerFrom = new PeerObject(msgPeerFrom);

                        switch (tag) {
                            case 1:
                                processEntryMessage(peerFrom);
                                outToPeer.write(createEntryReponseMsg(peerFrom));
                                break;
                            //Tag 2 wird in einer anderen Methode verarbeitet
                            case 3:
                                processNodeRequestMsg(peerFrom);
                                peerFrom.getOutToPeerStream().write(createNodeResponseMsg());
                                break;
                            case 4:
                                byte[] msg4 = inFromPeer.readNBytes(24);
                                processNodeResponeMessage(msg4);
                                break;
                            case 5:
                                processIAmAliveMsg(peerFrom);
                                break;
                            case 6:
                                byte[] msg6 = inFromPeer.readNBytes(4);
                                processNodeSearchMsg(peerFrom, msg6);
                                break;
                            case 7:
                                byte[] msg7 = inFromPeer.readNBytes(2);
                                processIAmFoundMsg(peerFrom, msg7);
                                break;
                            case 8:
                                byte[] msg8 = inFromPeer.readNBytes(2);
                                processMsgMsg(peerFrom, msg8, inFromPeer);
                                break;
                            case 9:
                                outToPeer.write(createIAmAliveMsg());
                                processAreYouAliveMsg(peerFrom);
                                break;
                            case 10:
                                processIAmLeaderMsg(peerFrom);
                                break;
                            default:
                                peerUtilities.switchDefault();
                        }

                        connectionSocket.close();

                        peerUtilities.printMetaInfo(peerFrom, tag, version);

                    } catch (ConnectException c) {
                        peerUtilities.connectException(c);
                    } catch (Exception ioe) {
                        peerUtilities.errorMessage(ioe);
                    }

                });
                mainThread.start();
            }

        } catch (SocketException s) {
            peerUtilities.printLogInformation("Socket geschlossen");
            peerUtilities.errorMessage(s);
            //SocketExceptions werden abgefangen, da diese nur geworfen werden, wenn der Socket durch exit geschlossen wird
        } catch (Exception e) {
            peerUtilities.errorMessage(e);
        }
    }

    private byte[] createEntryMsg() {

        byte[] entryMsg = new byte[8];
        entryMsg[0] = (byte) 1; // Tag
        entryMsg[1] = (byte) 0; // Version
        packPeerPackage(entryMsg, 2, myPeer);
        return entryMsg;
    }

    private void processEntryMessage(PeerObject p) {

        p.setId(Utilities.charToByteArray((char) (++idCounter)));
        addPeer(p);
    }

    private byte[] createEntryReponseMsg(PeerObject p) {

        byte[] msg = new byte[36];
        msg[0] = (byte) 2; // Tag
        msg[1] = (byte) 1; // Version

        byte[] newId = p.getId();
        msg[2] = newId[0];
        msg[3] = newId[1];

        for (int i = 0; i < 4; i++) {

            PeerObject o = new PeerObject();

            if (getPeerList().size() > i + 1)
                o = getPeerList().get(i + 1);

            packPeerPackage(msg, i * peerUtilities.getPeerPackLength() + 4, o);
        }

        return msg;
    }

    private void processEntryResponeMessage(PeerObject p, byte[] entyResponeMessage) {

        int tag = entyResponeMessage[0];
        int version = entyResponeMessage[1];

        peerUtilities.printMetaInfo(p, tag, version);

        byte[] id = Arrays.copyOfRange(entyResponeMessage, 2, 4);
        myPeer.setId(id);

        int maxAnfragen = 1;

        for (int i = 4, j = 0; i < entyResponeMessage.length; i += peerUtilities.getPeerPackLength()) {

            byte[] peerPack = Arrays.copyOfRange(entyResponeMessage, i, i + peerUtilities.getPeerPackLength());

            if (!peerUtilities.isArrayEmty(peerPack)) {

                PeerObject po = new PeerObject(peerPack);
                addPeer(po);
                if (j < maxAnfragen && sendMsg(po, createNodeRequestMsg()))
                    j++;
                po.closeStreams();
            }
        }
    }

    private byte[] createNodeRequestMsg() {
        byte[] msg = new byte[10];
        msg[0] = (byte) 3; // Tag
        msg[1] = (byte) 1; // Version
        packPeerPackage(msg, 2, myPeer);
        return msg;
    }

    private void processNodeRequestMsg(PeerObject p) {
        addPeer(p);
    }

    public byte[] createNodeResponseMsg() {

        byte[] msg = new byte[34];
        msg[0] = (byte) 4; // Tag
        msg[1] = (byte) 1; // Version
        packPeerPackage(msg, 2, myPeer);

        for (int i = 1; i < 4; i++) {

            PeerObject o = new PeerObject();

            if (getPeerList().size() > i + 1)
                o = getPeerList().get(i + 1);

            packPeerPackage(msg, i * peerUtilities.getPeerPackLength() + 2, o);
        }

        return msg;
    }

    private void processNodeResponeMessage(byte[] msg) {

        for (int i = 0; i < msg.length; i += peerUtilities.getPeerPackLength()) {

            byte[] peerPack = Arrays.copyOfRange(msg, i, i + peerUtilities.getPeerPackLength());

            if (!peerUtilities.isArrayEmty(peerPack)) {
                PeerObject po = new PeerObject(peerPack);
                addPeer(po);
            }
        }
    }

    private byte[] createIAmAliveMsg() {

        byte[] msg = new byte[10];
        msg[0] = (byte) 5; // Tag
        msg[1] = (byte) 1; // Version
        packPeerPackage(msg, 2, myPeer);
        return msg;
    }

    private void processIAmAliveMsg(PeerObject p) {
        addPeer(p);
    }

    private byte[] createNodeSearchMsg(int destId) {
        byte[] nodeSearchMsg = new byte[14];
        nodeSearchMsg[0] = (byte) 6; // Tag
        nodeSearchMsg[1] = (byte) 1; // Version
        packPeerPackage(nodeSearchMsg, 2, myPeer);
        byte[] searchID = peerUtilities.charToByteArray((char) ++searchIDCounter);
        byte[] destID = peerUtilities.charToByteArray((char) destId);
        nodeSearchMsg[10] = searchID[0];
        nodeSearchMsg[11] = searchID[1];
        nodeSearchMsg[12] = destID[0];
        nodeSearchMsg[13] = destID[1];

        searchList.add(new SearchObject(myPeer.getIp(), myPeer.getPort(), myPeer.getId(), searchID, destID));
        return nodeSearchMsg;
    }

    private void processNodeSearchMsg(PeerObject p, byte[] msg) {

        SearchObject incomingSearch = new SearchObject(p, msg);

        for (SearchObject so : searchList) {
            // Ich habe die Msg schon einmal empfangen.
            if (so.getIdAsInt() == incomingSearch.getIdAsInt() && incomingSearch.getSearchIdAsInt() == so.getSearchIdAsInt()) {
                //peerUtilities.println(gui, myPeer.getPortAsInt() + " hat den Search schonmal erhalten.");
                return;
            }
        }

        searchList.add(incomingSearch);

        // Fall 1 Ich bin der gesuchte -> rückantwort
        if (incomingSearch.getDestIdAsInt() == myPeer.getIdAsInt()) {
            peerUtilities.printLogInformation("Ich bin der gesuchte Peer");
            sendMsg(incomingSearch, createIAmFoundMsg(incomingSearch.getSearchId()));
            incomingSearch.closeStreams();
            return;
        }

        // Fall 2 Ich bin nicht der gesuchte & ich habe noch nie die Msg empfangen -> weiterleiten
        byte[] meta = {(byte) 6, (byte) 1};
        byte[] newMsg = peerUtilities.addAll(meta, peerUtilities.addAll(p.getPeerPackage(), msg));
        for (PeerObject po : getPeerList()) {
            sendMsg(po, newMsg);
            po.closeStreams();
            peerUtilities.printLogInformation("SearchID " + incomingSearch.getSearchIdAsInt() + " wurde an ID " + po.getIdAsInt() + " weitergeleitet");
        }
    }

    private byte[] createIAmFoundMsg(byte[] searchId) {
        byte[] iAmFoundMsg = new byte[12];
        iAmFoundMsg[0] = 7; // Tag
        iAmFoundMsg[1] = 1; // Version
        packPeerPackage(iAmFoundMsg, 2, myPeer);
        iAmFoundMsg[10] = searchId[0];
        iAmFoundMsg[11] = searchId[1];

        return iAmFoundMsg;
    }

    // Ip-Port entnehmen und in Liste speichern.
    private void processIAmFoundMsg(PeerObject p, byte[] msg) {

        int searchId = peerUtilities.byteArrayToInt(msg);
        peerUtilities.printLogInformation("Antwort auf SearchID " + searchId + " erhalten");
        addPeer(p);
    }

    private byte[] createMsgMsg(String txt) {

        byte[] msgHeader = new byte[10];
        msgHeader[0] = 8; // Tag
        msgHeader[1] = 1; // Version
        packPeerPackage(msgHeader, 2, myPeer);

        byte[] msgText = txt.getBytes();
        byte[] msgLength = peerUtilities.charToByteArray((char) msgText.length);

        int arrLength = 12 + msgText.length;

        byte[] msgMsg = Arrays.copyOf(msgHeader, arrLength);
        msgMsg[10] = msgLength[0];
        msgMsg[11] = msgLength[1];

        System.arraycopy(msgText, 0, msgMsg, 12, msgText.length);

        return msgMsg;
    }

    private void processMsgMsg(PeerObject p, byte[] msg, InputStream inFromPeer) throws Exception {

        addPeer(p);
        int length = peerUtilities.byteArrayToInt(msg);

        byte[] msgMsg = inFromPeer.readNBytes(length);
        String txt = new String(msgMsg);
        int id = p.getIdAsInt();
        peerUtilities.printMsg("[Von ID " + id + "] " + txt);
    }

    private byte[] createAreYouAliveMsg() {
        byte[] alive = new byte[10];
        alive[0] = 9;//Tag
        alive[1] = 1;//Version
        packPeerPackage(alive, 2, myPeer);
        return alive;
    }

    private void processAreYouAliveMsg(PeerObject p) {
        addPeer(p);
        startLeaderElection(false);
    }

    private byte[] createIAmLeaderMsg() {
        byte[] leader = new byte[10];
        leader[0] = 10;//Tag
        leader[1] = 1;//Version
        packPeerPackage(leader, 2, myPeer);
        return leader;
    }

    private void processIAmLeaderMsg(PeerObject p) {
        peerUtilities.setLeader(p.getIdAsInt());
    }

    private boolean isServer() {
        return peerUtilities.getServerIp().equals("localhost") && myPeer.getPortAsInt() == peerUtilities.getStandardPort();
    }

    /**
     * Wenn die Methode benutzt wird, müssen danach die Streams manuell geschlossen
     * werden.
     *
     * @param po  Peer, an dem die Msg gesendet werden soll
     * @param msg Msg, die versendet werden soll
     * @return boolean, ob das Versenden erfolgreich gewesen ist
     */
    private boolean sendMsg(PeerObject po, byte[] msg) {
        try {
            po.getOutToPeerStream().write(msg);
            return true;
        } catch (Exception e) {
            // peerUtilities.errorMessage(e);
            return false;
        }
    }

    @SuppressWarnings("deprecation")
    public void exit() {
        try {
            myServer.close();
            keepAlive.stop();
            if (isServer)
                cleanPeerList.stop();
        } catch (Exception e) {
            peerUtilities.errorMessage(e);
        }

    }

    public void packPeerPackage(byte[] destination, int offset, PeerObject p) {

        try {

            int numberOfBytes = (destination.length > offset + 6) ? 8 : 6;
            byte[] peerPacket = p.getPeerPackage();

            for (int i = 0; i < peerPacket.length && i < numberOfBytes; i++)
                destination[offset + i] = peerPacket[i];

        } catch (Exception e) {
            peerUtilities.errorMessage(e);
        }
    }

    public void deletePeersFromPeerList(ArrayList<PeerObject> delete) {

        for (PeerObject p : delete) {
            modifyPeerList(REMOVE, p);
        }
    }

    public void modifyPeerList(int action, PeerObject insertORemove) {

        synchronized (myPeer) {
            switch (action) {
                case INSERT:
                    peerList.add(0, insertORemove);
                    break;
                case REMOVE:
                    peerList.remove(insertORemove);
                    break;
                default:
                    peerUtilities.switchDefault();
            }

        }
    }

    public void addPeer(PeerObject peerObject) {

        try {
            for (PeerObject p : getPeerList()) {
                if (Arrays.equals(p.getId(), peerObject.getId())) {
                    p.updateTimestamp();
                    reducePeerList();
                    return;
                }
            }
            modifyPeerList(INSERT, peerObject);
            reducePeerList();
        } catch (Exception e) {
            peerUtilities.modificationException(e);
        }
    }

    private void reducePeerList() {

        try {
            ArrayList<PeerObject> deleteList = new ArrayList<>();
            ArrayList<PeerObject> peerList = getPeerList();

            for (PeerObject p : peerList) {
                if (peerList.indexOf(p) >= 4)
                    deleteList.add(p);
            }
            deletePeersFromPeerList(deleteList);
        } catch (Exception e) {
            peerUtilities.modificationException(e);
        }
    }

    public PeerObject getPeerObject(int id, int timeoutInMs) {

        startSearch(id);
        boolean inMyList = false;
        PeerObject p = null;
        long startMs = new Date().getTime();
        while ((!inMyList) && new Date().getTime() - startMs < timeoutInMs) {
            try {
                Thread.sleep(10);
                p = getPeerObjectFromList(id);
                inMyList = (p != null);
            } catch (Exception e) {
                peerUtilities.errorMessage(e);
            }
        }
        return p;
    }

    private PeerObject getPeerObjectFromList(int id) {

        for (PeerObject p : getPeerList()) {

            if (id == p.getIdAsInt()) {
                return p;
            }
        }
        return null;
    }

    public void startSearch(int destId) {
        try {
            if (isPeerInMyList(destId))
                return;

            if (destId == myPeer.getIdAsInt()) {
                addPeer(myPeer);
                return;
            }

            for (PeerObject po : getPeerList()) {
                sendMsg(po, createNodeSearchMsg(destId));
                po.closeStreams();
            }
        } catch (Exception e) {
            peerUtilities.modificationException(e);
        }
    }

    public void startLeaderElection(boolean showProgressBar) {

        peerUtilities.setLeader(-1);
        peerUtilities.printLogInformation("Leader-Election gestartet");

        int numberOfPeers = Variables.getIntValue("max_peers_in_network");
        int timeout = 1000 + (50 * numberOfPeers);

        SwingWorker<Integer, Integer> sw = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() {

                ProgressBar progressBar = new ProgressBar(gui);
                if (showProgressBar) {
                    progressBar.setVisible(true);
                    progressBar.setProgressBar(0);
                }

                Thread t = new Thread(() -> {
                    try {
                        boolean iAmLeader = true;
                        for (int i = myPeer.getIdAsInt() + 1; i <= numberOfPeers; i++) {

                            if (!iAmLeader)
                                return;

                            if (showProgressBar)
                                progressBar.setProgressBar(i * 100 / (2 * numberOfPeers));

                            PeerObject pS = getPeerObject(i, timeout);

                            if (pS != null) {
                                iAmLeader = !findLeader(i, pS, showProgressBar, progressBar, timeout);
                            }
                        }

                        peerUtilities.printLogInformation("Ich bin Leader");
                        peerUtilities.setLeader(myPeer.getIdAsInt());

                        for (int i = myPeer.getIdAsInt() - 1; i >= 0; i--) {
                            PeerObject p = getPeerObject(i, timeout);
                            if (p != null) {
                                sendMsg(p, createIAmLeaderMsg());
                                p.closeStreams();
                            }
                            if (showProgressBar)
                                progressBar.setProgressBar(100 - (i * 100 / (2 * numberOfPeers)));
                        }

                        if (showProgressBar)
                            progressBar.dispose();

                    } catch (Exception e) {
                        peerUtilities.errorMessage(e);
                    }
                });
                t.start();
                return 0;
            }
        };
        sw.execute();
    }

    private boolean findLeader(int i, PeerObject pS, boolean showProgressBar, ProgressBar progressBar, int timeout) {

        AtomicBoolean isLeaderReacheble = new AtomicBoolean(false);

        try {

            ExecutorService service = Executors.newSingleThreadExecutor();
            Future<Void> future = service.submit(() -> {

                try {
                    PeerObject p = pS.clone();
                    sendMsg(p, createAreYouAliveMsg());
                    byte[] respose = p.getInFromPeerStream().readNBytes(10);
                    p.closeStreams();
                    PeerObject p1 = new PeerObject(Arrays.copyOfRange(respose, 2, 10));
                    if (p1.getIdAsInt() == i) {
                        peerUtilities.printLogInformation("Antwort auf Tag 9 erhalten");
                        processIAmAliveMsg(p1);
                        isLeaderReacheble.set(true);
                        if (showProgressBar) {
                            while (gui.getLeaderId() == -1) {
                                int percent = 100 * (progressBar.getWert() + 1) / 99;
                                if (percent >= 100) {
                                    JOptionPane.showMessageDialog(progressBar, "Leader-Election war nicht erfolgreich", "Fehler", JOptionPane.ERROR_MESSAGE);
                                    progressBar.dispose();
                                    return null;
                                } else {
                                    progressBar.setProgressBar(percent);
                                    Thread.sleep(timeout);
                                }
                            }
                            progressBar.setProgressBar(100);
                            Thread.sleep(500);
                            progressBar.dispose();
                        }
                        return null;
                    }
                } catch (Exception e) {
                    modifyPeerList(REMOVE, pS);
                    //peerUtilities.errorMessage(e);
                    //Es kann sein, dass ein PeerObjekt nicht erreichbar ist
                    //Diese Fehlermeldung wird hier abgefangen
                }
                return null;
            });
            future.get(timeout, TimeUnit.MILLISECONDS);

        } catch (TimeoutException e) {
            peerUtilities.printLogInformation("Timeout bei ID " + i);
        } catch (Exception e) {
            peerUtilities.errorMessage(e);
        }

        return isLeaderReacheble.get();
    }

    private boolean isPeerInMyList(int peerId) {

        return getPeerObjectFromList(peerId) != null;
    }

    public void sendMsg(String txt, int idInput) {

        Thread t = new Thread(() -> {
            peerUtilities.printLogInformation("[An ID " + idInput + "] " + txt);
            int timeout = 3000;
            PeerObject p = getPeerObject(idInput, timeout);

            if (p == null) {
                peerUtilities.printMsg("ID: " + idInput + " konnte nicht gefunden werden.");
                return;
            }
            peerUtilities.printMsg("[An ID " + idInput + "] " + txt);

            byte[] ip = null;
            byte[] port = null;
            byte[] id = null;

            for (PeerObject peerObject : getPeerList()) {
                if (peerObject.getIdAsInt() == idInput) {
                    ip = peerObject.getIp();
                    port = peerObject.getPort();
                    id = peerObject.getId();
                }
            }

            PeerObject po = new PeerObject(ip, port, id);
            sendMsg(po, createMsgMsg(txt));
            po.closeStreams();
        });
        t.start();
    }

    public ArrayList<PeerObject> getPeerList() {
        synchronized (myPeer) {
            return new ArrayList<>(peerList);
        }
    }

    public Gui getGui() {
        return gui;
    }

    public PeerObject getMyPeer() {
        return myPeer;
    }

    public PeerUtilities getPeerUtilities() {
        return peerUtilities;
    }

    public static void main(String[] args) {
        Peer p = new Peer(3439);
        p.startPeer();
    }
}