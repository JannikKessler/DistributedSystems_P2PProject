import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class Peer {

    // Geteilte Attribute
    private PeerObject entryServer;
    private ArrayList<PeerObject> peerList;
    private ArrayList<SearchObject> searchList;
    private int searchIDCounter = 0;
    private Gui gui;
    private Thread keepAlive;
    private boolean exit = false;
    private boolean isServer = false;
    private PeerObject myPeer;
    private Point location;

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
        peerList = new ArrayList<>();
        searchList = new ArrayList<>();
        myPeer = new PeerObject(Utilities.getMyIpAsByteArray(),
            Utilities.charToByteArray((char) ((port == -1) ? Utilities.getStandardPort() : port)), new byte[2]);
        isServer = isServer();
        this.location = location;
    }

    public void startPeer() {

        try {

            BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
            Utilities.printMyIp();

            if (Utilities.isShowGui()) {
                gui = new Gui(isServer, location, this);
                AnzeigeThread at = new AnzeigeThread(gui, peerList);
                at.start();
            }

            if (!isServer) {

                entryServer = new PeerObject(Utilities.getServerIpAsByteArray(), Utilities.getStandardPortAsByteArray(),
                    new byte[2]);

                System.out.println("Client gestartet");

                byte[] entryMsg = createEntryMsg();
                while (!sendMsg(entryServer, entryMsg)) {
                    System.err.println("EntryServer konnte nicht erreicht werden\nVersuche es erneut...");
                    Thread.sleep(1000);
                }

                byte[] entyResponeMessage = new byte[36];
                entryServer.getInFromPeerStream().read(entyResponeMessage, 0, 36);
                processEntryResponeMessage(entryServer, entyResponeMessage);
                entryServer.closeStreams();

            } else {
                System.out.println("Server gestartet");

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

                            if (exit)
                                return;
                        }
                    } catch (Exception e) {
                        Utilities.errorMessage(e);
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
                        if (exit)
                            return;
                    } catch (Exception e) {
                        Utilities.errorMessage(e);
                    }
                }
            });
            keepAlive.start();

            gui.setHeadline((isServer ? "Server" : "Peer") + " " + myPeer.getIpAsString() + ":"
                + myPeer.getPortAsInt() + "; " + myPeer.getIdAsInt());

            ServerSocket myServer = new ServerSocket(myPeer.getPortAsInt());

            while (true) {

                Socket connectionSocket = myServer.accept();

                Thread t = new Thread(() -> {

                    try {

                        InputStream inFromPeer = connectionSocket.getInputStream();
                        OutputStream outToPeer = connectionSocket.getOutputStream();

                        byte[] msg = new byte[1];

                        inFromPeer.read(msg, 0, 1);
                        int tag = msg[0];

                        inFromPeer.read(msg, 0, 1);
                        int version = msg[0];

                        msg = new byte[8];
                        inFromPeer.read(msg, 0, 8);
                        PeerObject peerFrom = new PeerObject(msg);
                        Utilities.printMetaInfo(gui, peerFrom, tag, version);

                        switch (tag) {
                            case 1:
                                processEntryMessage(peerFrom);
                                outToPeer.write(createEntryReponseMsg(peerFrom));
                                break;
                            // Case 2 wird in einer anderen Methode verarbeitet
                            case 3:
                                processNodeRequestMsg(peerFrom);
                                peerFrom.getOutToPeerStream().write(createNodeResponseMsg());
                                break;
                            case 4:
                                msg = new byte[24];
                                inFromPeer.read(msg, 0, 24);
                                processNodeResponeMessage(msg);
                                break;
                            case 5:
                                processIAmAliveMsg(peerFrom);
                                break;
                            case 6:
                                msg = new byte[4];
                                inFromPeer.read(msg, 0, 4);
                                processNodeSearchMsg(peerFrom, msg);
                                break;
                            case 7:
                                msg = new byte[2];
                                inFromPeer.read(msg, 0, 2);
                                processIAmFoundMsg(peerFrom, msg);
                                break;
                            case 8:
                                msg = new byte[2];
                                inFromPeer.read(msg, 0, 2);
                                processMsgMsg(peerFrom, msg, inFromPeer);
                                break;
                            case 9:
                                //TODO
                                break;
                            case 10:
                                //TODO
                                break;
                            default:
                                Utilities.switchDefault();
                        }

                        connectionSocket.close();

                    } catch (Exception ioe) {
                        Utilities.errorMessage(ioe);
                    }

                });
                t.start();
            }

        } catch (Exception e) {
            Utilities.errorMessage(e);
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

            packPeerPackage(msg, i * Utilities.getPeerPackLength() + 4, o);
        }

        return msg;
    }

    private void processEntryResponeMessage(PeerObject p, byte[] entyResponeMessage) {

        int tag = entyResponeMessage[0];
        int version = entyResponeMessage[1];

        Utilities.printMetaInfo(gui, p, tag, version);

        byte[] id = Arrays.copyOfRange(entyResponeMessage, 2, 4);
        myPeer.setId(id);

        for (int i = 4; i < entyResponeMessage.length; i += Utilities.getPeerPackLength()) {

            byte[] peerPack = Arrays.copyOfRange(entyResponeMessage, i, i + Utilities.getPeerPackLength());

            if (!Utilities.isArrayEmty(peerPack)) {

                PeerObject po = new PeerObject(peerPack);
                if (sendMsg(po, createNodeRequestMsg()))
                    addPeer(po);
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

        for (int i = 0; i < 4; i++) {

            PeerObject o = new PeerObject();

            if (getPeerList().size() > i + 1)
                o = getPeerList().get(i + 1);

            packPeerPackage(msg, i * Utilities.getPeerPackLength() + 2, o);
        }

        return msg;
    }

    private void processNodeResponeMessage(byte[] msg) {

        for (int i = 0; i < msg.length; i += Utilities.getPeerPackLength()) {

            byte[] peerPack = Arrays.copyOfRange(msg, i, i + Utilities.getPeerPackLength());

            if (!Utilities.isArrayEmty(peerPack)) {
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
        byte[] searchID = Utilities.charToByteArray((char) ++searchIDCounter);
        byte[] destID = Utilities.charToByteArray((char) destId);
        nodeSearchMsg[10] = searchID[0];
        nodeSearchMsg[11] = searchID[1];
        nodeSearchMsg[12] = destID[0];
        nodeSearchMsg[13] = destID[1];

        searchList.add(new SearchObject(myPeer.getIp(), myPeer.getPort(), myPeer.getId(), searchID, destID));
        return nodeSearchMsg;
    }

    private void processNodeSearchMsg(PeerObject p, byte[] msg) {

        SearchObject incomingSearch = new SearchObject(p, msg);

        // Fall 1 Ich bin der gesuchte -> rückantwort
        if (incomingSearch.getDestIdAsInt() == myPeer.getIdAsInt()/* searchList.get(i).getDestId() */) {
            //System.out.println("Erfolg!!!");
            Utilities.println(gui, "Ich bin der gesuchte Peer");
            PeerObject po = new PeerObject(incomingSearch.getIp(), incomingSearch.getPort(), incomingSearch.getId());
            sendMsg(po, createIAmFoundMsg(incomingSearch.getSearchId()));
            po.closeStreams();
            //System.out.println(incomingSearch.getIdAsInt() + " hat " + myPeer.getIdAsInt() /* searchList.get(i).getDestId() */ + " gefunden");
        }

        for (SearchObject so : searchList) {
            // Ich habe die Msg schon einmal empfangen.
            if (Arrays.equals(incomingSearch.getIp(), so.getIp()) && incomingSearch.getPortAsInt() == so.getPortAsInt()
                && incomingSearch.getSearchIdAsInt() == so.getSearchIdAsInt()) {
                //System.out.println(myPeer.getPortAsInt() + " hat den Search schonmal erhalten.");
                return;
            }
        }

        searchList.add(incomingSearch);

        // Fall 2 Ich bin nicht der gesuchte & ich habe noch nie die Msg empfangen ->
        // weiterleiten
        for (PeerObject po : getPeerList()) {
            //System.out.println(myPeer.getPortAsInt() + " schickt Search an " + po.getPortAsInt());
            byte[] meta = {(byte) 6, (byte) 1};
            byte[] newMsg = Utilities.addAll(meta, Utilities.addAll(p.getPeerPackage(), msg));
            sendMsg(po, newMsg);
            po.closeStreams();
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

        int id = Utilities.byteArrayToChar(msg);

        if (id == myPeer.getIdAsInt())
            addPeer(p);
        else
            Utilities.errorMessage(new Exception("Nicht erwartete ID"));
    }

    private byte[] createMsgMsg(String txt) {
        byte[] msgHeader = new byte[10];
        msgHeader[0] = 8; // Tag
        msgHeader[1] = 1; // Version
        packPeerPackage(msgHeader, 2, myPeer);

        byte[] msgText = txt.getBytes();
        byte[] msgLength = Utilities.charToByteArray((char) msgText.length);

        int arrLength = 12 + msgText.length;

        byte[] msgMsg = Arrays.copyOf(msgHeader, arrLength);
        msgMsg[10] = msgLength[0];
        msgMsg[11] = msgLength[1];

        for (int i = 0; i < msgText.length; i++) {
            msgMsg[i + 12] = msgText[i];
        }

        return msgMsg;
    }

    private void processMsgMsg(PeerObject p, byte[] msg, InputStream inFromPeer) throws Exception {

        addPeer(p);
        int length = Utilities.byteArrayToChar(msg);

        byte[] msgMsg = new byte[length];
        inFromPeer.read(msgMsg, 0, length);
        String txt = new String(msgMsg);
        int id = p.getIdAsInt();
        Utilities.printMsg(gui, "[Von ID " + id + "] " + txt);
    }

    private byte[] createAreYouAliveMsg() {
        return new byte[0];
    }

    private int processAreYouAliveMsg(byte[] msg, OutputStream outToPeer) {
        //Verarbeiten der Msg
        //Erstellen einer Tag 5 Msg und senden
        return -1; //gibt id des Peers zurück, der mir das Packet gesendet hat
    }

    private byte[] createIAmLeaderMsg() {
        return new byte[0];
    }

    private int processIAmLeaderMsg() {
        Utilities.setLeader(-1);
        return -1;
    }

    private boolean isServer() {
        return Utilities.getServerIp().equals("localhost") && myPeer.getPortAsInt() == Utilities.getStandardPort();
    }

    /**
     * Wenn die Methode benutzt wird, müssen danach die Streams manuell geschlossen
     * werden.
     *
     * @param po
     * @param msg
     * @return
     */
    private boolean sendMsg(PeerObject po, byte[] msg) {
        try {
            po.getOutToPeerStream().write(msg);
            return true;
        } catch (Exception e) {
            // Utilities.errorMessage(e);
            return false;
        }
    }

    public void exit() {
        exit = true;
    }

    public void packPeerPackage(byte[] destination, int offset, PeerObject p) {

        try {

            int numberOfBytes = (destination.length > offset + 6) ? 8 : 6;
            byte[] peerPacket = p.getPeerPackage();

            for (int i = 0; i < peerPacket.length && i < numberOfBytes; i++)
                destination[offset + i] = peerPacket[i];

        } catch (Exception e) {
            Utilities.errorMessage(e);
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
                    Utilities.switchDefault();
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
            Utilities.modificationException(e);
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
            Utilities.modificationException(e);
        }
    }

    //TODO Emanuel
    public PeerObject getPeerObject(int id) {
        return null;
    }

    public void startSearch(int destId) {
        try {
            if (isPeerInMyList(destId))
                return;

            for (PeerObject po : getPeerList()) {
                sendMsg(po, createNodeSearchMsg(destId));
                po.closeStreams();
            }
        } catch (Exception e) {
            Utilities.modificationException(e);
        }
    }

    public void startLeaderElection() {

        Thread t = new Thread(() -> {
            //
        });
        t.start();
        //TODO Wird aus der Gui aufgerufen
    }

    private boolean isPeerInMyList(int peerId) {
        try {
            for (PeerObject po : getPeerList()) {
                if (po.getIdAsInt() == peerId)
                    return true;
            }
        } catch (Exception e) {
            Utilities.modificationException(e);
        }
        return false;
    }

    public void sendMsg(String txt, int idInput) {

        Utilities.println(gui, "[An ID " + idInput + "] " + txt);

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
    }

    public ArrayList<PeerObject> getPeerList() {
        synchronized (myPeer) {
            return new ArrayList<>(peerList);
        }
    }

    public static void main(String[] args) {
        Peer p = new Peer(3439);
        p.startPeer();
    }
}
