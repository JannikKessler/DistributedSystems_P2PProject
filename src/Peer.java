import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;


public class Peer {

    //Geteilte Attribute
    private PeerObject entryServer;
    private ArrayList<PeerObject> peerList;
    private ArrayList<SearchObject> searchList; //TODO implementieren
    private int searchIDCounter = 0;
    private Gui gui;
    private Thread keepAlive;
    private boolean exit = false;
    private boolean isServer = false;
    private PeerObject myPeer;
    private Point location;

    //Server Attribute
    private int idCounter = 0;
    private Thread cleanPeerList;

    //FINALs
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
        searchList = new ArrayList<SearchObject>();
        myPeer = new PeerObject(
            Utilities.getMyIpAsByteArray(),
            Utilities.charToByteArray((char) ((port == -1) ? Utilities.getStandardPort() : port)),
            new byte[2]);
        isServer = isServer();
        this.location = location;
    }

    public void startPeer() {

        try {

            BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
            Utilities.printMyIp();

            if (!isServer) {

                entryServer = new PeerObject(Utilities.getServerIpAsByteArray(), Utilities.getStandardPortAsByteArray(), new byte[2]);

                System.out.println("Client gestartet");

                byte[] entryMsg = createEntryMsg();
                while (!sendMsg(entryServer, entryMsg)) {
                    System.err.println("EntryServer konnte nicht erreicht werden\nVersuche es erneut...");
                    Thread.sleep(1000);
                }

                byte[] entyResponeMessage = new byte[36];
                entryServer.getInFromPeerStream().read(entyResponeMessage, 0, 36);
                processEntryResponeMessage(entyResponeMessage);
                entryServer.closeStreams();

                keepAlive = new Thread(() -> {
                    while (true) {
                        try {
                            byte[] msg = createIAmAliveMsg();
                            sendMsg(entryServer, msg);
                            entryServer.closeStreams();
                            Thread.sleep(Variables.getIntValue("time_send_keep_alive"));
                            if (Utilities.byteArrayToChar(myPeer.getId()) == 6) {
                            	System.out.println("Suche von: " + (int)Utilities.byteArrayToChar(myPeer.getId()));
                            	startSearch(1);
                            }
                            if (exit)
                                return;
                        } catch (Exception e) {
                            Utilities.errorMessage(e);
                        }
                    }
                });
                keepAlive.start();
                
                

            } else {
                System.out.println("Server gestartet");

                cleanPeerList = new Thread(() -> {
                    try {
                        while (true) {
                            long grenzwert = new Date().getTime() - Variables.getIntValue("time_server_max_without_keep_alive");
                            ArrayList<PeerObject> deleteList = new ArrayList<>();
                            for (PeerObject p : peerList) {
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

            gui = new Gui((isServer ? "Server" : "Peer") + " " + myPeer.getIpAsString() + ":" + myPeer.getPortAsInt() + "; " + myPeer.getIdAsInt(), location, this);
            AnzeigeThread at = new AnzeigeThread(gui, peerList);
            at.start();

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

                        System.out.println(myPeer.getPortAsInt() + ">> " + tag + " <<");

                        switch (tag) {
                            case 1:
                                msg = new byte[6];
                                inFromPeer.read(msg, 0, 6);
                                outToPeer.write(createEntryReponseMsg(processEntryMessage(msg)));
                                break;
                            //Case 2 wird in einer anderen Methode verarbeitet
                            case 3:
                                msg = new byte[8];
                                inFromPeer.read(msg, 0, 8);
                                PeerObject p = processNodeRequestMsg(msg);
                                p.getOutToPeerStream().write(createNodeResponseMsg());
                                break;
                            case 4:
                                msg = new byte[32];
                                inFromPeer.read(msg, 0, 32);
                                processNodeResponeMessage(msg);
                                break;
                            case 5:
                                msg = new byte[8];
                                inFromPeer.read(msg, 0, 8);
                                processIAmAliveMsg(msg);
                                break;
                            case 6:
                                //TODO NodeSearchMsg
                                msg = new byte[14];
                                inFromPeer.read(msg, 0, 14);
                                processNodeSearchMsg(msg);
                                break;
                            case 7:
                                //TODO IAmFound
                                msg = new byte[10];
                                inFromPeer.read(msg, 0, 10);
                                processIAmFoundMsg(msg);
                                break;
                            case 8:
                                //TODO MsgMsg
                                msg = new byte[0];
                                processMsgMsg(msg);
                                break;
                            default:
                                Utilities.switchDefault();
                        }
                        //if (myPeer.getIdAsInt() == 1) {
                        	//System.out.println(myPeer.getIpAsString() + ":" + myPeer.getIdAsInt() + " sucht gerade.");
                        //}

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
        entryMsg[0] = (byte) 1; //Tag
        entryMsg[1] = (byte) 0; //Version
        packPeerPackage(entryMsg, 2, myPeer.getIp(), myPeer.getPort(), myPeer.getId());
        return entryMsg;
    }

    private PeerObject processEntryMessage(byte[] msg) {
        PeerObject p = new PeerObject(msg, ++idCounter);
        addPeer(p);
        return p;
    }

    private byte[] createEntryReponseMsg(PeerObject p) {

        byte[] msg = new byte[36];
        msg[0] = (byte) 2; //Tag
        msg[1] = (byte) 1; //Version

        byte[] newId = p.getId();
        msg[2] = newId[0];
        msg[3] = newId[1];

        for (int i = 0; i < 4; i++) {

            PeerObject o = null;

            if (peerList.size() > i + 1)
                o = peerList.get(i + 1);

            byte[] ip = {0, 0, 0, 0};
            byte[] port = {0, 0};
            byte[] id = {0, 0};
            if (o != null) {
                ip = o.getIp();
                port = o.getPort();
                id = o.getId();

                //System.out.println("test1234: " + (int) Utilities.byteArrayToChar(id));
            }
            packPeerPackage(msg, i * Utilities.getPeerPackLength() + 4, ip, port, id);
            // Utilities.printByteArrayAsBinaryCode(msg);
        }

        return msg;
    }

    private void processEntryResponeMessage(byte[] entyResponeMessage) {

        int tag = entyResponeMessage[0];
        int version = entyResponeMessage[1];

        System.out.println(tag);

        if (!(tag == 2)) {
            Utilities.fehlermeldungBenutzerdefiniert("Fehlerhafter tag übergeben: " + tag);
            return;
        }

        if (!(version == 1)) {
            Utilities.fehlermeldungVersion();
            return;
        }

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
        msg[0] = (byte) 3; //Tag
        msg[1] = (byte) 1; //Version
        packPeerPackage(msg, 2, myPeer.getIp(), myPeer.getPort(), myPeer.getId());
        return msg;
    }

    private PeerObject processNodeRequestMsg(byte[] msg) {

        PeerObject p = new PeerObject(msg);
        addPeer(p);

        return p;
    }

    public byte[] createNodeResponseMsg() {

        byte[] msg = new byte[34];
        msg[0] = (byte) 4; //Tag
        msg[1] = (byte) 1; //Version

        for (int i = 0; i < 4; i++) {

            PeerObject o = null;

            if (peerList.size() > i + 1)
                o = peerList.get(i + 1);

            byte[] ip = {0, 0, 0, 0};
            byte[] port = {0, 0};
            byte[] id = {0, 0};
            if (o != null) {
                ip = o.getIp();
                port = o.getPort();
                id = o.getId();
            }
            packPeerPackage(msg, i * Utilities.getPeerPackLength() + 2, ip, port, id);
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
        msg[0] = (byte) 5; //Tag
        msg[1] = (byte) 1; //Version
        packPeerPackage(msg, 2, myPeer.getIp(), myPeer.getPort(), myPeer.getId());
        return msg;
    }

    private void processIAmAliveMsg(byte[] msg) {
        addPeer(new PeerObject(msg));
    }

    //TODO Wo aufgerufen, muss es an alle Nachbarn geschickt werden.
    private byte[] createNodeSearchMsg(int destId) {
        //TODO
    	byte[] nodeSearchMsg = new byte[14];
    	nodeSearchMsg[0] = (byte) 6; //Tag
    	nodeSearchMsg[1] = (byte) 1; //Version
    	packPeerPackage(nodeSearchMsg, 2, myPeer.getIp(), myPeer.getPort(), myPeer.getId());
    	byte[] searchID = Utilities.charToByteArray((char) searchIDCounter++);
    	byte[] destID = Utilities.charToByteArray((char) destId);						//TODO Testsuche anpassen
    	nodeSearchMsg[10] = searchID[0];
    	nodeSearchMsg[11] = searchID[1];
    	nodeSearchMsg[12] = destID[0];
    	nodeSearchMsg[13] = destID[1];
    	
    	searchList.add(new SearchObject(myPeer.getIp(), myPeer.getPort(), myPeer.getId(), searchID, destID));
        return nodeSearchMsg;
    }

    private void processNodeSearchMsg(byte[] nodeSearchMsg) {
    	SearchObject incomingSearch = new SearchObject(nodeSearchMsg);

    	//Fall 1 Ich bin der gesuchte -> rückantwort
		if (Utilities.byteArrayToChar(incomingSearch.getDestId()) == Utilities.byteArrayToChar(myPeer.getId()/*searchList.get(i).getDestId()*/)) {
			System.out.println("Erfolg!!!");
			PeerObject po = new PeerObject(incomingSearch.getIp(), incomingSearch.getPort(), incomingSearch.getId());
			sendMsg(po, createIAmFoundMsg(incomingSearch.getSearchId()));
			po.closeStreams();
			System.out.println(Utilities.byteArrayToChar(incomingSearch.getId()) + " hat " + 
					Utilities.byteArrayToChar(myPeer.getId()/*searchList.get(i).getDestId()*/) + " gefunden");
		}
    	
    	for (int i = 0; i < searchList.size(); i++) {
    		//Ich habe die Msg schon einmal empfangen.
    		if (Utilities.byteArrayToChar(incomingSearch.getIp()) == Utilities.byteArrayToChar(searchList.get(i).getIp()) 
    				&& Utilities.byteArrayToChar(incomingSearch.getPort()) == Utilities.byteArrayToChar(searchList.get(i).getPort())
    				&& Utilities.byteArrayToChar(incomingSearch.getSearchId()) == Utilities.byteArrayToChar(searchList.get(i).getSearchId())) {
    			System.out.println(myPeer.getPortAsInt() + " hat den Search schonmal erhalten.");
    			return;
    		}
    	}
    	//searchList.add(incomingSearch);
    	
        //Fall 2 Ich bin nicht der gesuchte & ich habe noch nie die Msg empfangen -> weiterleiten
    	for (int i = 0; i < peerList.size(); i++) {
    		PeerObject po = peerList.get(i);
    		System.out.println(myPeer.getPortAsInt() + " schickt Search an " + po.getPortAsInt());
    		/* Es scheint so, dass myPeer und po vertauscht sind. Die Peers aus der eigenen Liste schicken dem 'Eigenen' Peer die SearchAnfrage. */
    		sendMsg(po, createNodeSearchMsg((int)Utilities.byteArrayToChar(incomingSearch.getDestId())));
    		po.closeStreams();
    	}
    }

    //TODO Schickt die eigene ODER vom Suchenden Source ID mit?
    private byte[] createIAmFoundMsg(byte[] searchId) {
    	byte[] iAmFoundMsg = new byte[12];
    	iAmFoundMsg[0] = 7; //Tag
    	iAmFoundMsg[1] = 1; //Version
    	packPeerPackage(iAmFoundMsg, 2, myPeer.getIp(), myPeer.getPort(), myPeer.getId());
    	iAmFoundMsg[10] = searchId[0];
    	iAmFoundMsg[11] = searchId[1];
    	
        return iAmFoundMsg;
    }

    //Ip-Port entnehmen und in Liste speichern.
    private void processIAmFoundMsg(byte[] msg) {
        //TODO Wird der neue Peer auf deleteList gesetzt?
    	byte[] destIp = new byte[4];
    	byte[] destPort = new byte[2];
    	byte[] destId = new byte[2];
    	int offset = 0;
    	for (int i = 0; i < destIp.length; i++)
    		destIp[i] = msg[i];
    	offset += destIp.length;
    	for (int j = 0; j < destPort.length; j++)
    		destPort[j] = msg[j + offset];
    	offset += destPort.length;
    	for (int k = 0; k < destId.length; k++)
    		destId[k] = msg[k + offset];
    	
    	peerList.add(new PeerObject(destIp, destPort, destId));
    }


    private byte[] createMsgMsg() {
        //TODO
        return null;
    }

    private void processMsgMsg(byte[] msg) {
        //TODO Verarbeitet die Msg
        //Gibt ergebnis in die Gui aus
    }


    private boolean isServer() {
        return Utilities.getServerIp().equals("localhost") && myPeer.getPortAsInt() == Utilities.getStandardPort();
    }

    /**
     * Wenn die Methode benutzt wird, müssen danach die Streams manuell geschlossen werden.
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
            //Utilities.errorMessage(e);
            return false;
        }
    }

    public void exit() {
        exit = true;
    }

    public void packPeerPackage(byte[] destination, int offset, byte[] ip, byte[] port, byte[] id) {

        try {

            for (int i = 0; i < ip.length; i++)
                destination[offset + i] = ip[i];

            for (int i = 0; i < port.length; i++)
                destination[offset + i + ip.length] = port[i];

            if (destination.length > offset + ip.length + port.length) {
                for (int i = 0; i < id.length; i++)
                    destination[offset + i + ip.length + port.length] = id[i];
                //System.out.println("hihi");
            }

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

        for (PeerObject p : peerList) {
            if (Arrays.equals(p.getId(), peerObject.getId())) {
                p.updateTimestamp();
                reducePeerList();
                return;
            }
        }
        modifyPeerList(INSERT, peerObject);
        reducePeerList();
    }

    private void reducePeerList() {
        ArrayList<PeerObject> deleteList = new ArrayList<>();

        for (PeerObject p : peerList) {
            if (peerList.indexOf(p) >= 4)
                deleteList.add(p);
        }
        deletePeersFromPeerList(deleteList);
    }

    public void startSearch(int destId) {
        //TODO Wir von Gui aufgerufen um Suche zu starten
    	for (int i = 0; i < peerList.size(); i++) {
    		PeerObject po = peerList.get(i);
    		sendMsg(po, createNodeSearchMsg(destId));
    		po.closeStreams();
    	}
    }

    public void sendMsg() {
        //TODO Wird von Gui aufgerufen
        createMsgMsg();
    }


    public static void main(String[] args) {
        Peer p = new Peer(3342);
        p.startPeer();
    }
}
