package serviceregistry;

/*
 * Decompiled with CFR 0_118.
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

class SOASocketListener
extends Thread {
    private final int INFO = 1;
    private final int DEBUG = 0;
    private final int UNDEFINED = 0;
    private final int REG_TEAM = 1;
    private final int UNREG_TEAM = 2;
    private final int QUERY_TEAM = 3;
    private final int PUB_SERVICE = 4;
    private final int QUERY_SERVICE = 5;
    private final int MIN_SEC_LEVEL = 1;
    private final int MAX_SEC_LEVEL = 3;
    private final String fieldSeparator = "|";
    private Connection dbase;
    private boolean errOnUnreachIP;
    private boolean errOnUnconnectedPort;
    private boolean teamTestOwn;
    private boolean pubService;
    private boolean mchSegmentFound;
    private int msgCount;
    private int msgSocket;
    private int errorCode;
    private int whichCommand;
    private int timeout;
    private int timeoutMessageCount;
    private int numSpooledMessages;
    private char startMsgChar;
    private char endMsgChar;
    private char endSegmentChar;
    private String msgSuccess;
    private String msgFailure;
    private boolean isTestMode = false;
    private boolean terminateOnNACK = false;
    private boolean isFirstSegment = false;
    private String msgReady;
    private String msgBye;
    private String nameOfFeedingSystem;
    private String currSegment;
    private String finalGoodOutputDirectory;
    private String finalBadOutputDirectory;
    private String tempOutputDirectory;
    private String currMsgResponse;
    private String errorMessage;
    private String okContent;
    private String okBody;
    private String teamName;
    private String teamID;
    private String tagName;
    private String teamExpire;
    private String launchDate;
    private String dbaseDriver;
    private String dbaseURL;
    private String dbaseUser;
    private String dbasePasswd;
    private String spooledMessage;
    private Properties soaListenerProperties;
    private SimpleDateFormat FORMAT_TIMESTAMP = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat FILENAME_GENERATOR = new SimpleDateFormat("yyyyMMddHHmmssSS");

    public SOASocketListener(String string) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        this.msgCount = 0;
        this.soaListenerProperties = new Properties();
        try {
            String string2 = string + File.separator + "soa.msgListener.properties";
            FileInputStream fileInputStream = new FileInputStream(string2);
            this.soaListenerProperties.load(fileInputStream);
            fileInputStream.close();
            this.launchDate = simpleDateFormat.format(new Date());
        }
        catch (Exception var6_5) {
            this.getClass();
            this.LogMsg(var6_5, "SOASocketListener : Error while accessing the SOA properties file (soa.msgListener.properties)", 1);
        }
        String string3 = this.soaListenerProperties.getProperty("IncomingListenerPort", "3128");
        try {
            this.msgSocket = Integer.parseInt(string3);
        }
        catch (Exception var6_6) {
            this.msgSocket = 3128;
        }
        String string4 = this.soaListenerProperties.getProperty("TeamTimeout", "NEVER");
        if (string4.compareTo("NEVER") == 0) {
            this.timeout = -1;
        } else {
            try {
                this.timeout = Integer.parseInt(string4);
            }
            catch (Exception var6_7) {
                this.timeout = 5;
            }
        }
        string4 = this.soaListenerProperties.getProperty("CheckTimeoutAfterNumMessages", "10");
        try {
            this.timeoutMessageCount = Integer.parseInt(string4);
        }
        catch (Exception var6_8) {
            this.timeoutMessageCount = 10;
        }
        this.teamTestOwn = false;
        string4 = this.soaListenerProperties.getProperty("TeamTestOwn", "NO");
        if (string4.compareTo("YES") == 0) {
            this.teamTestOwn = true;
        }
        this.errOnUnreachIP = false;
        string4 = this.soaListenerProperties.getProperty("ErrOnUnreachableIP", "NO");
        if (string4.compareTo("YES") == 0) {
            this.errOnUnreachIP = true;
        }
        this.errOnUnconnectedPort = false;
        string4 = this.soaListenerProperties.getProperty("ErrOnNonActivePublishLocation", "NO");
        if (string4.compareTo("YES") == 0) {
            this.errOnUnconnectedPort = true;
        }
        this.nameOfFeedingSystem = this.soaListenerProperties.getProperty("NameOfSOASystem", "Misys");
        this.msgReady = this.soaListenerProperties.getProperty("ReadyForNewMessageMessage", "NONE");
        this.msgBye = this.soaListenerProperties.getProperty("ClientTerminatingMessage", "NONE");
        this.finalGoodOutputDirectory = this.soaListenerProperties.getProperty("SOAListenerGoodOutputDir", "." + File.separator + "Good" + File.separator);
        this.finalBadOutputDirectory = this.soaListenerProperties.getProperty("SOAListenerBadOutputDir", "." + File.separator + "Bad" + File.separator);
        this.tempOutputDirectory = this.soaListenerProperties.getProperty("SOAListenerTemporaryDir", "." + File.separator);
        this.msgSuccess = this.soaListenerProperties.getProperty("MessageReceived", "AA");
        this.msgFailure = this.soaListenerProperties.getProperty("MessageNotReceived", "AE");
        this.startMsgChar = 11;
        this.endMsgChar = 28;
        this.endSegmentChar = 13;
        string4 = this.soaListenerProperties.getProperty("TestMessageMode", "NO");
        if (string4.compareTo("YES") == 0) {
            this.isTestMode = true;
        }
        if ((string4 = this.soaListenerProperties.getProperty("TerminateOnNACK", "NO")).compareTo("YES") == 0) {
            this.terminateOnNACK = true;
        }
        string4 = this.soaListenerProperties.getProperty("SOAListenerLogDir", "NO");
        this.dbaseDriver = this.soaListenerProperties.getProperty("SOAListenerJDBCDriver", "NO-JDBC");
        this.dbaseURL = this.soaListenerProperties.getProperty("SOAListenerDBaseURL", "NO-DBASE");
        this.dbaseUser = this.soaListenerProperties.getProperty("SOAListenerDBaseUser", "NO-DBASE-USER");
        this.dbasePasswd = this.soaListenerProperties.getProperty("SOAListenerDBasePasswd", "NO-DBASE-PASSWD");
    }

    @Override
    public void run() {
        this.whoStartedMe();
        while (this.startListen() > 0) {
        }
        System.exit(-1);
    }

    public void whoStartedMe() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String string = "";
        String string2 = this.soaListenerProperties.getProperty("SOAListenerLogRollover", "NO");
        if (string2.compareTo("YES") == 0) {
            int n;
            Date date = new Date();
            int n2 = date.getYear() + 1900;
            int n3 = date.getMonth() + 1;
            int n4 = date.getDate();
            int n5 = date.getHours();
            int n6 = date.getMinutes();
            this.getClass();
            this.LogMsg(null, "SOASocketListener : Rolling Over LISTENER Log", 1);
            --n5;
            n5 += 24;
            if (--n4 == 0) {
                n = 31;
                this.getClass();
                this.LogMsg(null, "SOASocketListener :   ==> Back to the Previous Month", 1);
                if (--n3 == 0) {
                    n3 = 12;
                }
                if (n3 == 9 || n3 == 4 || n3 == 6 || n3 == 11) {
                    n = 30;
                }
                if (n3 == 2) {
                    boolean bl = false;
                    if (n2 % 4 == 0) {
                        bl = true;
                    }
                    if (n2 % 100 == 0) {
                        bl = false;
                    }
                    if (n2 % 2000 == 0) {
                        bl = true;
                    }
                    n = 28;
                    if (bl) {
                        n = 29;
                    }
                }
                n4 += n;
                if (n3 == 12) {
                    --n2;
                }
            }
            string = this.soaListenerProperties.getProperty("SOAListenerLogDir", "../logs/") + File.separator + "SOARegisterListener.log";
            string = string + "." + Integer.toString(n2) + "-";
            if (n3 < 10) {
                string = string + "0";
            }
            string = string + Integer.toString(n3) + "-";
            if (n4 < 10) {
                string = string + "0";
            }
            string = string + Integer.toString(n4);
            String string3 = this.soaListenerProperties.getProperty("SOAListenerLogDir", "../logs/") + File.separator + "SOARegisterListenerYesterday.log";
            File file = new File(string3);
            n = file.renameTo(new File(string)) ? 1 : 0;
            if (n == 0) {
                this.getClass();
                this.LogMsg(null, "SOASocketListener : Log File Rename Failed", 1);
            }
            if (n == 1) {
                this.getClass();
                this.LogMsg(null, "SOASocketListener : Log File Rename Passed", 1);
            }
        }
    }

    protected void openConnection() throws ClassNotFoundException, SQLException {
        Class.forName(this.dbaseDriver);
        this.dbase = DriverManager.getConnection(this.dbaseURL, this.dbaseUser, this.dbasePasswd);
    }

    protected Connection getDBase() {
        return this.dbase;
    }

    private void openDBase() {
        try {
            this.openConnection();
            this.getDBase().setAutoCommit(false);
        }
        catch (ClassNotFoundException var1_1) {
            this.getClass();
            this.LogMsg(null, "openDBase  :  >> Error while loading JDBC Driver [" + var1_1.getMessage() + "]", 1);
        }
        catch (SQLException var1_2) {
            this.getClass();
            this.LogMsg(null, "openDBase  :  >> Error while establishing DBase connection [" + var1_2.getMessage() + "]", 1);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void closeDBase(boolean bl) {
        if (bl) {
            try {
                this.getDBase().commit();
            }
            catch (SQLException var2_3) {
                this.getClass();
                this.LogMsg(null, "closeDBase :  >> Error during Commmit [" + var2_3.getMessage() + "]", 1);
            }
            finally {
                try {
                    this.getDBase().close();
                }
                catch (SQLException var2_4) {
                    this.getClass();
                    this.LogMsg(null, "closeDBase :  >> Error during Close [" + var2_4.getMessage() + "]", 1);
                }
            }
        }
        try {
            this.getDBase().rollback();
        }
        catch (SQLException var2_6) {
            this.getClass();
            this.LogMsg(null, "closeDBase :  >> Error during Rollback [" + var2_6.getMessage() + "]", 1);
        }
        finally {
            try {
                this.getDBase().close();
            }
            catch (SQLException var2_7) {
                this.getClass();
                this.LogMsg(null, "closeDBase :  >> Error during Close [" + var2_7.getMessage() + "]", 1);
            }
        }
    }

    public int startListen() {
        int n = 1;
        boolean bl = true;
        boolean bl2 = true;
        Socket socket = null;
        char[] arrc = new char[]{this.endSegmentChar};
        this.getClass();
        this.LogMsg(null, "-----------------------------------------------", 1);
        this.getClass();
        this.LogMsg(null, "   SOA Register Online ... " + this.launchDate + " ", 1);
        this.getClass();
        this.LogMsg(null, "-----------------------------------------------", 1);
        this.getClass();
        this.LogMsg(null, "  >>> SOARegisterListener Active - Listening for messages from " + this.nameOfFeedingSystem + " on Socket:" + this.msgSocket, 1);
        try {
            ServerSocket serverSocket = new ServerSocket(this.msgSocket);
            if (!this.isTestMode) {
                serverSocket.setReuseAddress(true);
            }
            socket = serverSocket.accept();
            if (!this.isTestMode) {
                socket.setKeepAlive(true);
            }
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
            while (bl) {
                try {
                    int n2;
                    this.getClass();
                    this.LogMsg(null, "startListen :       >> Rx/Tx Streams OPEN - (notDone=" + bl2 + ")", 1);
                    String string = bufferedReader.readLine();
                    if (this.timeout > 0 && this.msgCount % this.timeoutMessageCount == 0 && (n2 = this.checkForTimeouts()) > 0) {
                        this.getClass();
                        this.LogMsg(null, "startListen :         >> Found and removed " + n2 + " expired team(s)", 1);
                    }
                    if (string == null) {
                        this.getClass();
                        this.LogMsg(null, "startListen :         >> Failed to READ from socket - endOfStream reached", 1);
                    } else {
                        if (this.endSegmentChar == '\r') {
                            this.getClass();
                            this.LogMsg(null, "startListen :         >> Spooling Incoming Message", 1);
                            n2 = string.length();
                            this.getClass();
                            this.LogMsg(null, "startListen :           >> Appending MsgSegment : (" + string.charAt(0) + "," + string.charAt(1) + "," + string.charAt(2) + "," + string.charAt(3) + " ... " + string.charAt(n2 - 3) + "," + string.charAt(n2 - 2) + "," + string.charAt(n2 - 1) + ")", 0);
                            string = string + new String(arrc);
                            while (bl2) {
                                String string2 = bufferedReader.readLine();
                                int n3 = string2.length() - 1;
                                n2 = string2.length();
                                if (string2.charAt(n3) == this.endMsgChar) {
                                    if (n2 < 3) {
                                        this.getClass();
                                        this.LogMsg(null, "startListen :           >> Appending Last MsgSegment : length =" + n2, 0);
                                        if (n2 == 1) {
                                            this.getClass();
                                            this.LogMsg(null, "startListen :           >> Appending Last MsgSegment : (" + string2.charAt(0) + ")", 0);
                                        }
                                        if (n2 == 2) {
                                            this.getClass();
                                            this.LogMsg(null, "startListen :           >> Appending Last MsgSegment : (" + string2.charAt(0) + "," + string2.charAt(1) + ")", 0);
                                        }
                                    } else {
                                        this.getClass();
                                        this.LogMsg(null, "startListen :           >> Appending Last MsgSegment : (" + string2.charAt(0) + "," + string2.charAt(1) + " ... " + string2.charAt(n2 - 2) + "," + string2.charAt(n2 - 1) + ")", 0);
                                    }
                                    string = string + string2;
                                    bl2 = false;
                                    continue;
                                }
                                if (n2 < 3) {
                                    this.getClass();
                                    this.LogMsg(null, "startListen :           >> Appending Next MsgSegment : length =" + n2, 0);
                                    if (n2 == 1) {
                                        this.getClass();
                                        this.LogMsg(null, "startListen :           >> Appending Last MsgSegment : (" + string2.charAt(0) + ")", 0);
                                    }
                                    if (n2 == 2) {
                                        this.getClass();
                                        this.LogMsg(null, "startListen :           >> Appending Last MsgSegment : (" + string2.charAt(0) + "," + string2.charAt(1) + ")", 0);
                                    }
                                } else {
                                    this.getClass();
                                    this.LogMsg(null, "startListen :           >> Appending Next MsgSegment : (" + string2.charAt(0) + "," + string2.charAt(1) + " ... " + string2.charAt(n2 - 2) + "," + string2.charAt(n2 - 1) + ")", 0);
                                }
                                string = string + string2 + new String(arrc);
                            }
                        }
                        this.getClass();
                        this.LogMsg(null, "startListen :         >> Processing Data", 1);
                        if (string.charAt(0) == this.startMsgChar) {
                            n = this.processMessage(string);
                        } else {
                            this.getClass();
                            this.LogMsg(null, "startListen :           >> First Byte of Message is not a <StartOfMessage>", 1);
                        }
                    }
                    if (string != null) {
                        if (n == 0) {
                            this.getClass();
                            this.LogMsg(null, "startListen :           >> Message NOT RECEIVED", 1);
                            printWriter.println(this.currMsgResponse.toString());
                            printWriter.flush();
                            if (this.isTestMode) {
                                bl = false;
                                n = 0;
                            }
                            if (this.terminateOnNACK) {
                                bl = false;
                                n = 0;
                            } else {
                                bl = true;
                                n = 1;
                            }
                        } else if (this.msgReady.compareTo("NONE") != 0 && string.indexOf(this.msgReady) >= 0) {
                            this.getClass();
                            this.LogMsg(null, "startListen :           >> " + this.nameOfFeedingSystem + " checking if READY (Received \"msgReady\" message at index " + string.indexOf(this.msgReady) + " of the incoming stream", 1);
                            printWriter.println(this.msgSuccess);
                            printWriter.flush();
                        } else if (string.indexOf(this.msgBye) < 0 || this.msgBye.compareTo("NONE") == 0) {
                            this.getClass();
                            this.LogMsg(null, "startListen :           >> Sending response Message from " + this.nameOfFeedingSystem, 1);
                            this.getClass();
                            this.LogMsg(null, "startListen :              >> Response is ... \n------------------------------\n" + this.currMsgResponse.substring(1, this.currMsgResponse.length() - 2) + "\n------------------------------", 0);
                            ++this.msgCount;
                            printWriter.println(this.currMsgResponse);
                            printWriter.flush();
                        } else {
                            this.getClass();
                            this.LogMsg(null, "startListen :           >> " + this.nameOfFeedingSystem + " sent TERMINATE message", 1);
                            printWriter.println(this.msgFailure);
                            printWriter.flush();
                            bl = false;
                            n = 0;
                        }
                    } else {
                        this.getClass();
                        this.LogMsg(null, "startListen :           >> Incoming Message IS NULL", 1);
                        printWriter.println(this.msgFailure);
                        printWriter.flush();
                        if (this.isTestMode) {
                            bl = false;
                            n = 0;
                        }
                        if (this.terminateOnNACK) {
                            bl = false;
                            n = 0;
                        } else {
                            bl = true;
                            n = 1;
                        }
                    }
                    if (!bl) {
                        this.getClass();
                        this.LogMsg(null, "startListen :         >> Closing Rx/Tx Streams on this Message", 1);
                        printWriter.close();
                        bufferedReader.close();
                        if (this.isTestMode) {
                            socket.close();
                            serverSocket.close();
                        }
                    } else {
                        bl2 = true;
                    }
                }
                catch (Exception var12_13) {
                    this.getClass();
                    this.LogMsg(var12_13, "startListen :     >> **EXCEPTION**", 1);
                    try {
                        this.getClass();
                        this.LogMsg(null, "startListen :    >> Processing Exception - closing socket and reopening", 1);
                        printWriter.close();
                        bufferedReader.close();
                        socket.close();
                        serverSocket.close();
                        serverSocket = new ServerSocket(this.msgSocket);
                        if (!this.isTestMode) {
                            serverSocket.setReuseAddress(true);
                        }
                        socket = serverSocket.accept();
                        if (!this.isTestMode) {
                            socket.setKeepAlive(true);
                        }
                        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        printWriter = new PrintWriter(socket.getOutputStream(), true);
                        bl = true;
                        n = 1;
                    }
                    catch (Exception var13_15) {
                        this.getClass();
                        this.LogMsg(var13_15, "startListen :    >> Unable to close socket - further exception - exiting listener", 1);
                        bl = false;
                        n = 0;
                    }
                }
                printWriter.close();
                bufferedReader.close();
                socket.close();
                serverSocket.close();
                serverSocket = new ServerSocket(this.msgSocket);
                if (!this.isTestMode) {
                    serverSocket.setReuseAddress(true);
                }
                socket = serverSocket.accept();
                if (!this.isTestMode) {
                    socket.setKeepAlive(true);
                }
                bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                printWriter = new PrintWriter(socket.getOutputStream(), true);
            }
            this.getClass();
            this.LogMsg(null, "  >>> SOARegisterListener Inactive", 1);
            if (!this.isTestMode) {
                socket.close();
                serverSocket.close();
            }
        }
        catch (Exception var12_14) {
            this.getClass();
            this.LogMsg(var12_14, "startListen :     >> **EXCEPTION**", 1);
            try {
                this.getClass();
                this.LogMsg(null, "startListener :    >> Processing Exception - closing socket - exiting listener", 1);
                n = 0;
                if (socket != null) {
                    socket.close();
                }
            }
            catch (Exception var13_16) {
                this.getClass();
                this.LogMsg(var13_16, "startListen :    >> Unable to close socket - further exception - exiting listener", 1);
                n = 0;
            }
        }
        return n;
    }

    private String getDateAfterXMinutes(int n) {
        Calendar calendar = Calendar.getInstance();
        if (n != 0) {
            calendar.add(12, n);
        }
        String string = "" + calendar.get(1) + "-";
        string = calendar.get(2) + 1 < 10 ? string + "0" + (calendar.get(2) + 1) + "-" : string + (calendar.get(2) + 1) + "-";
        string = calendar.get(5) < 10 ? string + "0" + calendar.get(5) : string + calendar.get(5);
        return string;
    }

    private String getTimeAfterXMinutes(int n) {
        Calendar calendar = Calendar.getInstance();
        if (n != 0) {
            calendar.add(12, n);
        }
        String string = calendar.get(11) < 10 ? "0" + calendar.get(11) + ":" : "" + calendar.get(11) + ":";
        string = calendar.get(12) < 10 ? string + "0" + calendar.get(12) + ":" : string + calendar.get(12) + ":";
        string = calendar.get(13) < 10 ? string + "0" + calendar.get(13) : string + calendar.get(13);
        return string;
    }

    private int checkForTimeouts() {
        int n = 0;
        String string = "";
        String string2 = this.getDateAfterXMinutes(0);
        String string3 = this.getTimeAfterXMinutes(0);
        this.getClass();
        this.LogMsg(null, "checkForTimeouts :       >> Checking for team time-outs - anything before " + string2 + " at " + string3 + ".", 1);
        this.openDBase();
        try {
            string = "select teamID, teamName from Team where expirationDate<'" + string2 + "' or (expirationDate='" + string2 + "' and expirationTime <'" + string3 + "') order by teamID;";
            PreparedStatement preparedStatement = this.getDBase().prepareStatement(string);
            preparedStatement.execute();
            ResultSet resultSet = preparedStatement.getResultSet();
            while (resultSet.next()) {
                ++n;
                int n2 = Integer.parseInt(resultSet.getString(1));
                String string4 = resultSet.getString(2);
                this.getClass();
                this.LogMsg(null, "checkForTimeouts :          >> Team '" + string4 + "' (ID : " + n2 + ") has expired ... removing registration and services ...", 1);
                string = "delete from Response where serviceID in (select serviceID from Service where teamID=" + n2 + ");";
                PreparedStatement preparedStatement2 = this.getDBase().prepareStatement(string);
                preparedStatement2.execute();
                int n3 = preparedStatement2.getUpdateCount();
                this.getClass();
                this.LogMsg(null, "checkForTimeouts :            >> Removed " + n3 + " row(s) from the Response table belonging to team", 0);
                preparedStatement2.close();
                string = "delete from Argument where serviceID in (select serviceID from Service where teamID=" + n2 + ");";
                preparedStatement2 = this.getDBase().prepareStatement(string);
                preparedStatement2.execute();
                n3 = preparedStatement2.getUpdateCount();
                this.getClass();
                this.LogMsg(null, "checkForTimeouts :            >> Removed " + n3 + " row(s) from the Argument table belonging to team", 0);
                preparedStatement2.close();
                string = "delete from Service where teamID=" + n2 + ";";
                preparedStatement2 = this.getDBase().prepareStatement(string);
                preparedStatement2.execute();
                n3 = preparedStatement2.getUpdateCount();
                this.getClass();
                this.LogMsg(null, "checkForTimeouts :            >> Removed " + n3 + " row(s) from the Service table belonging to team", 0);
                preparedStatement2.close();
                string = "delete from Team where teamID=" + n2 + ";";
                preparedStatement2 = this.getDBase().prepareStatement(string);
                preparedStatement2.execute();
                n3 = preparedStatement2.getUpdateCount();
                this.getClass();
                this.LogMsg(null, "checkForTimeouts :            >> Removed " + n3 + " row(s) from the Team table belonging to team", 0);
                preparedStatement2.close();
            }
            preparedStatement.close();
            this.closeDBase(true);
        }
        catch (SQLException var11_11) {
            this.getClass();
            this.LogMsg(null, "checkForTimeouts :                >> Error executing SQL=[" + string + "] - error=[" + var11_11.getMessage() + "]", 1);
            this.errorMessage = "Error executing SQL=[" + string + "] - error=[" + var11_11.getMessage() + "]";
            this.errorCode = -5;
            this.closeDBase(false);
        }
        return n;
    }

    private boolean isValidTagName(String string) {
        boolean bl = false;
        if (string.compareTo("GIORP-TOTAL") == 0 || string.compareTo("PAYROLL") == 0 || string.compareTo("CAR-LOAN") == 0 || string.compareTo("POSTAL") == 0) {
            bl = true;
        }
        return bl;
    }

    private boolean isAlphaNumeric(String string) {
        boolean bl = true;
        for (int i = 0; i < string.length(); ++i) {
            char c = string.charAt(i);
            if (c < '0') {
                bl = false;
            }
            if (c > '9' && c < 'A') {
                bl = false;
            }
            if (c > 'Z' && c < 'a') {
                bl = false;
            }
            if (c <= 'z') continue;
            bl = false;
        }
        return bl;
    }

    private boolean isValidDatatype(String string) {
        boolean bl = false;
        if (string.compareToIgnoreCase("char") == 0) {
            bl = true;
        }
        if (string.compareToIgnoreCase("int") == 0) {
            bl = true;
        }
        if (string.compareToIgnoreCase("short") == 0) {
            bl = true;
        }
        if (string.compareToIgnoreCase("long") == 0) {
            bl = true;
        }
        if (string.compareToIgnoreCase("float") == 0) {
            bl = true;
        }
        if (string.compareToIgnoreCase("double") == 0) {
            bl = true;
        }
        if (string.compareToIgnoreCase("String") == 0) {
            bl = true;
        }
        return bl;
    }

    private boolean isValidIPFormat(String string) {
        boolean bl = false;
        bl = string.matches("[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}");
        return bl;
    }

    private boolean isValidIPAddress(String string) {
        boolean bl = false;
        try {
            InetAddress inetAddress = InetAddress.getByName(string);
            bl = inetAddress.isReachable(2000);
        }
        catch (UnknownHostException var4_4) {
            this.getClass();
            this.LogMsg(null, "processPubService :                  >> IP Address " + string + " is unreachable - " + var4_4, 1);
        }
        catch (IOException var4_5) {
            this.getClass();
            this.LogMsg(null, "processPubService :                  >> IP Address " + string + " is unreachable - " + var4_5, 1);
        }
        return bl;
    }

    private boolean isValidPublishLocation(String string, String string2) {
        boolean bl = false;
        try {
            InetAddress inetAddress = InetAddress.getByName(string);
            int n = Integer.parseInt(string2);
            Socket socket = new Socket(inetAddress, n);
            socket.close();
            bl = true;
        }
        catch (UnknownHostException var6_7) {
            this.getClass();
            this.LogMsg(null, "processPubService :                  >> Service Published on " + string + ":" + string2 + " is unreachable - " + var6_7, 1);
        }
        catch (IOException var6_8) {
            this.getClass();
            this.LogMsg(null, "processPubService :                  >> Service Published on " + string + ":" + string2 + " is unreachable - " + var6_8, 1);
        }
        return bl;
    }

    private boolean isValidArgtype(String string) {
        boolean bl = false;
        if (string.compareToIgnoreCase("mandatory") == 0) {
            bl = true;
        }
        if (string.compareToIgnoreCase("optional") == 0) {
            bl = true;
        }
        return bl;
    }

    private boolean isValidCommentCharacters(String string) {
        boolean bl = true;
        for (int i = 0; i < string.length(); ++i) {
            char c = string.charAt(i);
            if (c < ' ') {
                bl = false;
            }
            if (c > '!' && c < '$') {
                bl = false;
            }
            if (c > '$' && c < '(') {
                bl = false;
            }
            if (c > ')' && c < ',') {
                bl = false;
            }
            if (c == '-') {
                bl = false;
            }
            if (c > ':' && c < 'A') {
                bl = false;
            }
            if (c <= 'z') continue;
            bl = false;
        }
        return bl;
    }

    private boolean isSecLevelValid(String string) {
        boolean bl = true;
        if (string.compareTo("1") != 0 && string.compareTo("2") != 0 && string.compareTo("3") != 0) {
            bl = false;
        }
        return bl;
    }

    private boolean inValidRange(String string, int n, int n2) {
        boolean bl = true;
        try {
            int n3 = Integer.parseInt(string);
            if (n <= n2) {
                if (n3 < n || n3 > n2) {
                    bl = false;
                }
            } else if (n3 < n) {
                bl = false;
            }
        }
        catch (NumberFormatException var6_6) {
            bl = false;
        }
        return bl;
    }

    private int processMessage(String string) {
        String string2;
        int n = 0;
        int n2 = 1;
        PrintWriter printWriter = null;
        char[] arrc = new char[]{this.endSegmentChar};
        char[] arrc2 = new char[]{this.startMsgChar};
        char[] arrc3 = new char[]{this.endMsgChar};
        this.whichCommand = 0;
        this.isFirstSegment = true;
        this.currMsgResponse = "";
        this.errorMessage = "";
        this.errorCode = 0;
        this.okContent = "";
        this.okBody = "";
        this.pubService = false;
        this.mchSegmentFound = false;
        this.getClass();
        this.LogMsg(null, "processMessage :           >> Processing Incoming Message", 1);
        int n3 = 1;
        try {
            if (this.isFirstSegment) {
                string2 = this.tempOutputDirectory + File.separator + "currentMsg.soa";
                printWriter = new PrintWriter(new FileWriter(string2, false));
                this.getClass();
                this.LogMsg(null, "processMessage :              >> Opening DBase ...", 0);
                this.openDBase();
            }
            while (n3 >= 0) {
                int n4 = string.indexOf(this.endSegmentChar, n3);
                if (n4 <= n3) {
                    n3 = -1;
                    if (this.currMsgResponse.length() == 0) {
                        this.getClass();
                        this.LogMsg(null, "processMessage :               >> Failed to find first segment", 1);
                        continue;
                    }
                    this.getClass();
                    this.LogMsg(null, "processMessage :               >> Failed to find subsequent segment (" + n2 + ")", 1);
                    continue;
                }
                this.currSegment = string.substring(n3, n4);
                this.getClass();
                this.LogMsg(null, "processMessage :               >> Parsed out segment (" + this.currSegment + ")", 1);
                printWriter.println(this.currSegment);
                printWriter.flush();
                n += this.ParseSegment();
                if (string.length() <= n4 + 1) {
                    this.getClass();
                    this.LogMsg(null, "processMessage :                 >> Message doesn't contain EOM marker", 1);
                    n3 = -1;
                    this.errorMessage = "Message doesn't contain EOM marker";
                    this.errorCode = -1;
                    n += this.errorCode;
                    continue;
                }
                if (string.charAt(n4 + 1) == this.endMsgChar) {
                    this.getClass();
                    this.LogMsg(null, "processMessage :                 >> found EOM marker", 1);
                    n3 = -1;
                    if (!this.pubService || this.mchSegmentFound) continue;
                    this.getClass();
                    this.LogMsg(null, "parseSegment :                 >> Failed to find MCH directive in last segment", 1);
                    this.errorMessage = "MCH directive not in last message segment";
                    this.errorCode = -1;
                    n = 1;
                    continue;
                }
                n3 = n4 + 1;
            }
        }
        catch (IOException var13_11) {
            this.getClass();
            this.LogMsg(var13_11, "parseSegment :             >> Unable to open TEMPORARY file for writing", 1);
        }
        catch (Exception var13_12) {
            this.getClass();
            this.LogMsg(var13_12, "processData :           ** EXCEPTION**", 1);
        }
        printWriter.flush();
        printWriter.close();
        if (n == 0) {
            n = 1;
            this.getClass();
            this.LogMsg(null, "processMessage :               >> Incoming SOA Message Saved to Temporary File", 1);
            string2 = this.tempOutputDirectory + File.separator + "currentMsg.soa";
            String string3 = this.finalGoodOutputDirectory + File.separator + this.FILENAME_GENERATOR.format(new Date(System.currentTimeMillis())) + ".soa";
            File file = new File(string2);
            boolean bl = file.renameTo(new File(string3));
            if (!bl) {
                this.getClass();
                this.LogMsg(null, "processMessage :                 >> File (" + string2 + ") not renamed to (" + string3 + ")", 1);
            }
            if (bl) {
                this.getClass();
                this.LogMsg(null, "processMessage :                 >> File Renamed to (" + string3 + ")", 1);
            }
            this.currMsgResponse = new String(arrc2) + "SOA|OK|" + this.okContent + new String(arrc);
            if (this.okBody.length() > 0) {
                this.currMsgResponse = this.currMsgResponse + this.okBody;
            }
            this.currMsgResponse = this.currMsgResponse + new String(arrc3);
            this.getClass();
            this.LogMsg(null, "processMessage :                 >> Constructing OK Response", 1);
            this.getClass();
            this.LogMsg(null, "processMessage :                 >> Committing DBase transaction(s)", 1);
            this.closeDBase(true);
        } else {
            n = 0;
            this.getClass();
            this.LogMsg(null, "processMessage :               >> Incoming SOA Message FAILED to be Saved to Temporary File - moving to BAD location", 1);
            string2 = this.tempOutputDirectory + File.separator + "currentMsg.soa";
            String string4 = this.finalBadOutputDirectory + File.separator + this.FILENAME_GENERATOR.format(new Date(System.currentTimeMillis())) + ".soa";
            File file = new File(string2);
            boolean bl = file.renameTo(new File(string4));
            if (!bl) {
                this.getClass();
                this.LogMsg(null, "processMessage :                 >> File (" + string2 + ") not renamed to (" + string4 + ")", 1);
            }
            if (bl) {
                this.getClass();
                this.LogMsg(null, "processMessage :                 >> File Renamed to (" + string4 + ")", 1);
            }
            this.currMsgResponse = new String(arrc2) + "SOA|NOT-OK|" + Integer.toString(this.errorCode) + "|" + this.errorMessage + "|" + new String(arrc) + new String(arrc3);
            this.getClass();
            this.LogMsg(null, "processMessage :               >> Constructing NOT-OK Response", 1);
            this.getClass();
            this.LogMsg(null, "processMessage :                 >> (.. |" + Integer.toString(this.errorCode) + "|" + this.errorMessage + "| ..", 1);
            this.getClass();
            this.LogMsg(null, "processMessage :                 >> Rolling Back DBase transaction(s)", 1);
            this.closeDBase(false);
        }
        this.getClass();
        this.LogMsg(null, "processMessage :           >> Processing Complete", 0);
        return n;
    }

    private int ParseSegment() {
        int n = 0;
        char[] arrc = new char[]{this.endSegmentChar};
        this.getClass();
        this.LogMsg(null, "parseSegment :             >> Begin Parsing", 1);
        int n2 = 0;
        String string = this.currSegment.substring(0, 4);
        try {
            if (this.isFirstSegment) {
                this.isFirstSegment = false;
                this.getClass();
                this.LogMsg(null, "parseSegment :             >> Parsing Segment <" + string.toUpperCase() + ">", 1);
                if (string.compareTo("DRC|") == 0) {
                    this.getClass();
                    int n3 = this.currSegment.indexOf("|", n2 + 4);
                    if (n3 >= 0) {
                        String string2 = this.currSegment.substring(n2 + 4, n3);
                        this.getClass();
                        this.LogMsg(null, "parseSegment :             \t>> Found DRC Directive, parsed SOA Command <" + string2 + ">", 1);
                        if (string2.compareTo("REG-TEAM") == 0 || string2.compareTo("UNREG-TEAM") == 0 || string2.compareTo("QUERY-TEAM") == 0 || string2.compareTo("PUB-SERVICE") == 0 || string2.compareTo("QUERY-SERVICE") == 0 || string2.compareTo("EXEC-SERVICE") == 0) {
                            if (string2.compareTo("REG-TEAM") == 0) {
                                if (this.currSegment.compareTo("DRC|REG-TEAM|||") == 0) {
                                    this.whichCommand = 1;
                                } else {
                                    this.getClass();
                                    this.LogMsg(null, "parseSegment :                 >> DRC|REG-TEAM segment not according to Spec.", 1);
                                    this.errorMessage = "DRC/REG-TEAM segment not according to Spec.";
                                    this.errorCode = -2;
                                }
                            }
                            if (string2.compareTo("UNREG-TEAM") == 0) {
                                this.teamName = "";
                                this.teamID = "";
                                n2 = n3 + 1;
                                this.getClass();
                                n3 = this.currSegment.indexOf("|", n2);
                                if (n3 >= 0) {
                                    this.teamName = this.currSegment.substring(n2, n3);
                                }
                                n2 = n3 + 1;
                                this.getClass();
                                n3 = this.currSegment.indexOf("|", n2);
                                if (n3 >= 0) {
                                    this.teamID = this.currSegment.substring(n2, n3);
                                }
                                if (this.teamName.length() > 0 && this.teamID.length() > 0) {
                                    this.whichCommand = 2;
                                    n = this.ProcessUnregTeam();
                                } else {
                                    this.getClass();
                                    this.LogMsg(null, "parseSegment :                 >> DRC|UNREG-TEAM segment not according to Spec.", 1);
                                    this.errorMessage = "DRC/UNREG-TEAM segment not according to Spec.";
                                    this.errorCode = -2;
                                }
                            }
                            if (string2.compareTo("QUERY-TEAM") == 0) {
                                this.teamName = "";
                                this.teamID = "";
                                n2 = n3 + 1;
                                this.getClass();
                                n3 = this.currSegment.indexOf("|", n2);
                                if (n3 >= 0) {
                                    this.teamName = this.currSegment.substring(n2, n3);
                                }
                                n2 = n3 + 1;
                                this.getClass();
                                n3 = this.currSegment.indexOf("|", n2);
                                if (n3 >= 0) {
                                    this.teamID = this.currSegment.substring(n2, n3);
                                }
                                if (this.teamName.length() > 0 && this.teamID.length() > 0) {
                                    this.whichCommand = 3;
                                } else {
                                    this.getClass();
                                    this.LogMsg(null, "parseSegment :                 >> DRC|QUERY-TEAM segment not according to Spec.", 1);
                                    this.errorMessage = "DRC/QUERY-TEAM segment not according to Spec.";
                                    this.errorCode = -2;
                                }
                            }
                            if (string2.compareTo("QUERY-SERVICE") == 0) {
                                this.teamName = "";
                                this.teamID = "";
                                n2 = n3 + 1;
                                this.getClass();
                                n3 = this.currSegment.indexOf("|", n2);
                                if (n3 >= 0) {
                                    this.teamName = this.currSegment.substring(n2, n3);
                                }
                                n2 = n3 + 1;
                                this.getClass();
                                n3 = this.currSegment.indexOf("|", n2);
                                if (n3 >= 0) {
                                    this.teamID = this.currSegment.substring(n2, n3);
                                }
                                if (this.teamName.length() > 0 && this.teamID.length() > 0) {
                                    this.whichCommand = 5;
                                } else {
                                    this.getClass();
                                    this.LogMsg(null, "parseSegment :                 >> DRC|QUERY-SERVICE segment not according to Spec.", 1);
                                    this.errorMessage = "DRC/QUERY-SERVICE segment not according to Spec.";
                                    this.errorCode = -2;
                                }
                            }
                            if (string2.compareTo("PUB-SERVICE") == 0) {
                                this.teamName = "";
                                this.teamID = "";
                                n2 = n3 + 1;
                                this.getClass();
                                n3 = this.currSegment.indexOf("|", n2);
                                if (n3 >= 0) {
                                    this.teamName = this.currSegment.substring(n2, n3);
                                }
                                n2 = n3 + 1;
                                this.getClass();
                                n3 = this.currSegment.indexOf("|", n2);
                                if (n3 >= 0) {
                                    this.teamID = this.currSegment.substring(n2, n3);
                                }
                                if (this.teamName.length() > 0 && this.teamID.length() > 0) {
                                    this.whichCommand = 4;
                                    this.spooledMessage = "";
                                    this.numSpooledMessages = 0;
                                    this.pubService = true;
                                } else {
                                    this.getClass();
                                    this.LogMsg(null, "parseSegment :                 >> DRC|PUB-SERVICE segment not according to Spec.", 1);
                                    this.errorMessage = "DRC/PUB-SERVICE segment not according to Spec.";
                                    this.errorCode = -2;
                                }
                            }
                            if (string2.compareTo("EXEC-SERVICE") == 0) {
                                this.getClass();
                                this.LogMsg(null, "parseSegment :                 >> EXEC-SERVICE command not processed by SOA-Registry.", 1);
                                this.errorMessage = "EXEC-SERVICE command not processed by SOA-Registry.";
                                this.errorCode = -2;
                            }
                        } else {
                            this.getClass();
                            this.LogMsg(null, "parseSegment :                 >> SOA command <" + string2.toUpperCase() + "> - UNKNOWN", 1);
                            this.errorMessage = "SOA command <" + string2.toUpperCase() + "> - UNKNOWN";
                            this.errorCode = -1;
                        }
                    } else {
                        this.getClass();
                        this.LogMsg(null, "parseSegment :                 >> DRC directive has no embedded SOA command", 1);
                        this.errorMessage = "DRC directive has no embedded SOA command";
                        this.errorCode = -1;
                    }
                } else {
                    this.getClass();
                    this.LogMsg(null, "parseSegment :                 >> Failed to find DRC directive in first segment", 1);
                    this.errorMessage = "DRC directive not in first message segment";
                    this.errorCode = -1;
                }
            } else {
                if (this.whichCommand == 1) {
                    n = this.ProcessRegTeam();
                }
                if (this.whichCommand == 3) {
                    n = this.ProcessQueryTeam();
                }
                if (this.whichCommand == 5) {
                    n = this.ProcessQueryService();
                }
                if (this.whichCommand == 4) {
                    ++this.numSpooledMessages;
                    this.spooledMessage = this.spooledMessage + this.currSegment + new String(arrc);
                    if (string.compareTo("MCH|") == 0) {
                        n = this.ProcessPubService();
                        this.mchSegmentFound = true;
                    }
                }
            }
        }
        catch (Exception var15_7) {
            this.getClass();
            this.LogMsg(var15_7, "parseSegment :             ** EXCEPTION **", 1);
        }
        return this.errorCode;
    }

    private int ProcessRegTeam() {
        boolean bl = false;
        int n = 0;
        String string = this.currSegment.substring(0, 4);
        this.getClass();
        this.LogMsg(null, "processRegTeam :             >> Parsing Segment <" + string.toUpperCase() + ">", 1);
        if (string.compareTo("INF|") == 0) {
            this.getClass();
            int n2 = this.currSegment.indexOf("|", n + 4);
            if (n2 >= 0) {
                String string2 = this.currSegment.substring(n + 4, n2);
                if (this.currSegment.substring(n2).compareTo("|||") == 0) {
                    if (string2.length() > 0) {
                        PreparedStatement preparedStatement;
                        ResultSet resultSet;
                        boolean bl2 = false;
                        String string3 = "";
                        try {
                            string3 = "select teamID, expirationTime from Team where teamName='" + string2 + "';";
                            preparedStatement = this.getDBase().prepareStatement(string3);
                            preparedStatement.execute();
                            resultSet = preparedStatement.getResultSet();
                            if (!resultSet.next()) {
                                this.getClass();
                                this.LogMsg(null, "processRegTeam :                >> No team found with name <" + string2 + ">", 0);
                            } else {
                                this.getClass();
                                this.LogMsg(null, "processRegTeam :                >> Team <" + string2 + "> already registered", 0);
                                this.teamID = resultSet.getString(1);
                                this.teamExpire = resultSet.getString(2);
                                bl2 = true;
                            }
                            preparedStatement.close();
                        }
                        catch (SQLException var18_10) {
                            this.getClass();
                            this.LogMsg(null, "processRegTeam :                >> Error executing SQL=[" + string3 + "] - error=[" + var18_10.getMessage() + "]", 1);
                            this.errorMessage = "Error executing SQL=[" + string3 + "] - error=[" + var18_10.getMessage() + "]";
                            this.errorCode = -5;
                            bl2 = true;
                        }
                        if (!bl2) {
                            this.getClass();
                            this.getClass();
                            this.getClass();
                            int n3 = 1 + (int)(Math.random() * (double)(3 - 1 + 1));
                            String string4 = "";
                            String string5 = "";
                            if (this.timeout >= 0) {
                                string4 = this.getDateAfterXMinutes(this.timeout);
                                string5 = this.getTimeAfterXMinutes(this.timeout);
                            }
                            try {
                                string3 = "insert into Team (teamName, securityLevel, expirationDate, expirationTime) VALUES('" + string2 + "'," + n3 + ",'" + string4 + "','" + string5 + "');";
                                PreparedStatement preparedStatement2 = this.getDBase().prepareStatement(string3);
                                preparedStatement2.execute();
                                this.getClass();
                                this.LogMsg(null, "processRegTeam :                >> Inserted team <" + string2 + "> into DBase", 0);
                                preparedStatement2.close();
                                string3 = "select teamID, expirationTime from Team where teamName='" + string2 + "';";
                                preparedStatement = this.getDBase().prepareStatement(string3);
                                preparedStatement.execute();
                                resultSet = preparedStatement.getResultSet();
                                if (!resultSet.next()) {
                                    this.getClass();
                                    this.LogMsg(null, "processRegTeam :                >> No team found with name <" + string2 + "> - Insert FAILED", 0);
                                } else {
                                    this.getClass();
                                    this.LogMsg(null, "processRegTeam :                >> Team <" + string2 + "> registered successfully", 0);
                                    this.teamID = resultSet.getString(1);
                                    this.teamExpire = resultSet.getString(2);
                                    bl2 = true;
                                }
                                preparedStatement.close();
                            }
                            catch (SQLException var21_15) {
                                this.getClass();
                                this.LogMsg(null, "processRegTeam :                >> Error executing SQL=[" + string3 + "] - error=[" + var21_15.getMessage() + "]", 1);
                                this.errorMessage = "Error executing SQL=[" + string3 + "] - error=[" + var21_15.getMessage() + "]";
                                this.errorCode = -5;
                                bl2 = true;
                            }
                        }
                        if (bl2) {
                            this.okContent = this.teamID + "|" + this.teamExpire + "||";
                            this.okBody = "";
                        }
                    } else {
                        this.getClass();
                        this.LogMsg(null, "processRegTeam :                 >> Illegal teamName in INF segment.", 1);
                        if (this.errorMessage.length() == 0) {
                            this.errorMessage = "Illegal teamName in INF segment.";
                            this.errorCode = -3;
                        }
                    }
                } else {
                    this.getClass();
                    this.LogMsg(null, "processRegTeam :                 >> INF segment not according to Spec.", 1);
                    if (this.errorMessage.length() == 0) {
                        this.errorMessage = "INF segment not according to Spec.";
                        this.errorCode = -2;
                    }
                }
            } else {
                this.getClass();
                this.LogMsg(null, "processRegTeam :                 >> INF segment not according to Spec.", 1);
                if (this.errorMessage.length() == 0) {
                    this.errorMessage = "INF segment not according to Spec.";
                    this.errorCode = -2;
                }
            }
        } else {
            this.getClass();
            this.LogMsg(null, "processRegTeam :                 >> Failed to find INF directive in second segment", 1);
            if (this.errorMessage.length() == 0) {
                this.errorMessage = "INF directive not in second message segment";
                this.errorCode = -1;
            }
        }
        return this.errorCode;
    }

    private int ProcessUnregTeam() {
        boolean bl = false;
        boolean bl2 = false;
        String string = "";
        this.getClass();
        this.LogMsg(null, "processUnRegTeam :             >> Attempting to UNREGISTER team " + this.teamName + " (ID : " + this.teamID + ")", 1);
        try {
            string = "select teamID, expirationTime from Team where teamName='" + this.teamName + "' and teamID=" + this.teamID + ";";
            PreparedStatement preparedStatement = this.getDBase().prepareStatement(string);
            preparedStatement.execute();
            ResultSet resultSet = preparedStatement.getResultSet();
            if (!resultSet.next()) {
                this.getClass();
                this.LogMsg(null, "processUnRegTeam :               >> No team '" + this.teamName + "' (ID : " + this.teamID + ") found registered in DBase", 1);
                this.errorMessage = "No team '" + this.teamName + "' (ID : " + this.teamID + ") found registered in DBase";
                this.errorCode = -4;
                bl = true;
            }
            preparedStatement.close();
            if (!bl) {
                string = "delete from Response where serviceID in (select serviceID from Service where teamID=" + this.teamID + ");";
                PreparedStatement preparedStatement2 = this.getDBase().prepareStatement(string);
                preparedStatement2.execute();
                int n = preparedStatement2.getUpdateCount();
                this.getClass();
                this.LogMsg(null, "processUnRegTeam :                  >> Removed " + n + " row(s) from the Response table belonging to team '" + this.teamName + "' (ID : " + this.teamID + ")", 0);
                preparedStatement2.close();
                string = "delete from Argument where serviceID in (select serviceID from Service where teamID=" + this.teamID + ");";
                preparedStatement2 = this.getDBase().prepareStatement(string);
                preparedStatement2.execute();
                n = preparedStatement2.getUpdateCount();
                this.getClass();
                this.LogMsg(null, "processUnRegTeam :                  >> Removed " + n + " row(s) from the Argument table belonging to team '" + this.teamName + "' (ID : " + this.teamID + ")", 0);
                preparedStatement2.close();
                string = "delete from Service where teamID=" + this.teamID + ";";
                preparedStatement2 = this.getDBase().prepareStatement(string);
                preparedStatement2.execute();
                n = preparedStatement2.getUpdateCount();
                this.getClass();
                this.LogMsg(null, "processUnRegTeam :                  >> Removed " + n + " row(s) from the Service table belonging to team '" + this.teamName + "' (ID : " + this.teamID + ")", 0);
                preparedStatement2.close();
                string = "delete from Team where teamID=" + this.teamID + ";";
                preparedStatement2 = this.getDBase().prepareStatement(string);
                preparedStatement2.execute();
                n = preparedStatement2.getUpdateCount();
                this.getClass();
                this.LogMsg(null, "processUnRegTeam :                  >> Removed " + n + " row(s) from the Team table belonging to team '" + this.teamName + "' (ID : " + this.teamID + ")", 0);
                preparedStatement2.close();
            }
        }
        catch (SQLException var8_8) {
            this.getClass();
            this.LogMsg(null, "processUnRegTeam :                >> Error executing SQL=[" + string + "] - error=[" + var8_8.getMessage() + "]", 1);
            this.errorMessage = "Error executing SQL=[" + string + "] - error=[" + var8_8.getMessage() + "]";
            this.errorCode = -5;
            bl = true;
        }
        if (this.errorCode == 0) {
            this.okContent = "|||";
            this.okBody = "";
        }
        return this.errorCode;
    }

    private int ProcessQueryTeam() {
        boolean bl = false;
        int n = 0;
        String string = this.currSegment.substring(0, 4);
        this.getClass();
        this.LogMsg(null, "processQueryTeam :             >> Parsing Segment <" + string.toUpperCase() + ">", 1);
        if (string.compareTo("INF|") == 0) {
            String string2 = "";
            this.getClass();
            int n2 = this.currSegment.indexOf("|", n + 4);
            if (n2 >= 0) {
                string2 = this.currSegment.substring(n + 4, n2);
            }
            String string3 = "";
            n = n2 + 1;
            this.getClass();
            n2 = this.currSegment.indexOf("|", n);
            if (n2 >= 0) {
                string3 = this.currSegment.substring(n, n2);
            }
            String string4 = "";
            n = n2 + 1;
            this.getClass();
            n2 = this.currSegment.indexOf("|", n);
            if (n2 >= 0) {
                string4 = this.currSegment.substring(n, n2);
            }
            if (string2.length() > 0 && string3.length() > 0 && string4.length() > 0) {
                ResultSet resultSet;
                PreparedStatement preparedStatement;
                boolean bl2 = false;
                String string5 = "";
                String string6 = "";
                try {
                    string5 = "select t.teamID, s.securityLevel from Team t, Service s where t.teamName='" + this.teamName + "' and t.teamID=" + this.teamID + " and s.teamID=t.teamID and s.tagName='" + string4 + "';";
                    preparedStatement = this.getDBase().prepareStatement(string5);
                    preparedStatement.execute();
                    resultSet = preparedStatement.getResultSet();
                    if (!resultSet.next()) {
                        this.getClass();
                        this.LogMsg(null, "processQueryTeam :               >> Team '" + this.teamName + "' (ID : " + this.teamID + ") does not have service " + string4 + " registered in DBase", 1);
                        this.errorMessage = "Team '" + this.teamName + "' (ID : " + this.teamID + ") does not have service " + string4 + " registered in DBase";
                        this.errorCode = -4;
                        bl2 = true;
                    } else {
                        string6 = resultSet.getString(2);
                        if (string6 != null && string6.length() > 0) {
                            this.getClass();
                            this.LogMsg(null, "processQueryTeam :               >> Found service <" + string4 + "> for team '" + this.teamName + "' (secLevel=" + string6 + ")", 1);
                        } else {
                            this.getClass();
                            this.LogMsg(null, "processQueryTeam :               >> No service <" + string4 + "> for team '" + this.teamName + "' found in DBase", 1);
                            this.errorMessage = "No service <" + string4 + "> for team '" + this.teamName + "' found in DBase";
                            this.errorCode = -4;
                            bl2 = true;
                        }
                    }
                    preparedStatement.close();
                }
                catch (SQLException var19_13) {
                    this.getClass();
                    this.LogMsg(null, "processQueryTeam :                >> Error executing SQL=[" + string5 + "] - error=[" + var19_13.getMessage() + "]", 1);
                    this.errorMessage = "Error executing SQL=[" + string5 + "] - error=[" + var19_13.getMessage() + "]";
                    this.errorCode = -5;
                    bl2 = true;
                }
                if (!bl2) {
                    try {
                        string5 = "select teamID, securityLevel from Team where teamName='" + string2 + "' and teamID=" + string3 + " and securityLevel>=" + string6 + ";";
                        preparedStatement = this.getDBase().prepareStatement(string5);
                        preparedStatement.execute();
                        resultSet = preparedStatement.getResultSet();
                        if (!resultSet.next()) {
                            this.getClass();
                            this.LogMsg(null, "processQueryTeam :               >> Team <" + string2 + "> does not have adequate security level to run service " + string4, 1);
                            this.errorMessage = "Team <" + string2 + "> does not have adequate security level to run service " + string4;
                            this.errorCode = -4;
                            bl2 = true;
                        }
                        preparedStatement.close();
                    }
                    catch (SQLException var19_14) {
                        this.getClass();
                        this.LogMsg(null, "processQueryTeam :                >> Error executing SQL=[" + string5 + "] - error=[" + var19_14.getMessage() + "]", 1);
                        this.errorMessage = "Error executing SQL=[" + string5 + "] - error=[" + var19_14.getMessage() + "]";
                        this.errorCode = -5;
                        bl2 = true;
                    }
                }
            } else {
                this.getClass();
                this.LogMsg(null, "processQueryTeam :                 >> INF segment not according to Spec.", 1);
                if (string2.length() == 0) {
                    this.getClass();
                    this.LogMsg(null, "processQueryTeam :                   >> Calling teamName is blank.", 1);
                    if (this.errorMessage.length() == 0) {
                        this.errorMessage = "INF segment not according to Spec (calling teamName is BLANK).";
                        this.errorCode = -2;
                    }
                }
                if (string3.length() == 0) {
                    this.getClass();
                    this.LogMsg(null, "processQueryTeam :                   >> Calling teamID is blank.", 1);
                    if (this.errorMessage.length() == 0) {
                        this.errorMessage = "INF segment not according to Spec (calling teamID is BLANK).";
                        this.errorCode = -2;
                    }
                }
                if (string4.length() == 0) {
                    this.getClass();
                    this.LogMsg(null, "processQueryTeam :                   >> Requested tagName is blank.", 1);
                    if (this.errorMessage.length() == 0) {
                        this.errorMessage = "INF segment not according to Spec (requested tagName is BLANK).";
                        this.errorCode = -2;
                    }
                }
            }
        } else {
            this.getClass();
            this.LogMsg(null, "processQueryTeam :                 >> Failed to find INF directive in second segment", 1);
            if (this.errorMessage.length() == 0) {
                this.errorMessage = "INF directive not in second message segment";
                this.errorCode = -1;
            }
        }
        if (this.errorCode == 0) {
            this.okContent = "|||";
            this.okBody = "";
        }
        return this.errorCode;
    }

    private int ProcessPubService() {
        boolean bl = false;
        int n = -1;
        int n2 = -1;
        String string = "";
        int n3 = 0;
        int n4 = 0;
        int n5 = 1;
        int n6 = 1;
        this.getClass();
        this.LogMsg(null, "processPubService :             >> Parsing " + this.numSpooledMessages + " Segment(s)", 1);
        int n7 = 0;
        int n8 = -1;
        for (int i = 0; i < this.numSpooledMessages && this.errorCode == 0; ++i) {
            Object object;
            String string2;
            String string3;
            String string4;
            boolean bl2;
            int n9;
            String string5;
            PreparedStatement preparedStatement;
            String string6;
            PreparedStatement preparedStatement2;
            n7 = n8 + 1;
            n8 = this.spooledMessage.indexOf(this.endSegmentChar, n7);
            this.currSegment = this.spooledMessage.substring(n7, n8);
            int n10 = 0;
            int n11 = 0;
            String string7 = this.currSegment.substring(n11, 4);
            this.getClass();
            this.LogMsg(null, "processPubService :             >> Parsing Segment <" + string7.toUpperCase() + ">", 1);
            if (string7.compareTo("SRV|") == 0) {
                string4 = "";
                this.getClass();
                n9 = this.currSegment.indexOf("|", n11 + 4);
                if (n9 >= 0) {
                    string4 = this.currSegment.substring(n11 + 4, n9);
                }
                if (string4.length() > 0) {
                    ++n10;
                }
                string2 = "";
                n11 = n9 + 1;
                this.getClass();
                n9 = this.currSegment.indexOf("|", n11);
                if (n9 >= 0) {
                    string2 = this.currSegment.substring(n11, n9);
                }
                if (string2.length() > 0) {
                    ++n10;
                }
                string5 = "";
                n11 = n9 + 1;
                this.getClass();
                n9 = this.currSegment.indexOf("|", n11);
                if (n9 >= 0) {
                    string5 = this.currSegment.substring(n11, n9);
                }
                if (string5.length() > 0) {
                    ++n10;
                }
                string3 = "";
                n11 = n9 + 1;
                this.getClass();
                n9 = this.currSegment.indexOf("|", n11);
                if (n9 >= 0) {
                    string3 = this.currSegment.substring(n11, n9);
                }
                if (string3.length() > 0) {
                    ++n10;
                }
                String string8 = "";
                n11 = n9 + 1;
                this.getClass();
                n9 = this.currSegment.indexOf("|", n11);
                if (n9 >= 0) {
                    string8 = this.currSegment.substring(n11, n9);
                }
                if (string8.length() > 0) {
                    ++n10;
                }
                String string9 = "";
                n11 = n9 + 1;
                this.getClass();
                n9 = this.currSegment.indexOf("|", n11);
                if (n9 >= 0 && n11 != n9) {
                    string9 = this.currSegment.substring(n11, n9);
                }
                if (n10 >= 5) {
                    bl2 = false;
                    string6 = "";
                    String string10 = "";
                    if (!this.isValidTagName(string4)) {
                        this.getClass();
                        this.LogMsg(null, "processPubService :               >> Tagname (" + string4 + ") is not valid", 1);
                        this.errorMessage = "Tagname (" + string4 + ") is not valid";
                        this.errorCode = -2;
                        bl2 = true;
                    }
                    if (!bl2 && !this.isAlphaNumeric(string2)) {
                        this.getClass();
                        this.LogMsg(null, "processPubService :               >> ServiceName (" + string2 + ") contains invalid characters", 1);
                        this.errorMessage = "ServiceName (" + string2 + ") contains invalid characters";
                        this.errorCode = -2;
                        bl2 = true;
                    }
                    if (!bl2 && !this.isSecLevelValid(string5)) {
                        this.getClass();
                        this.LogMsg(null, "processPubService :               >> Security Level (" + string5 + ") contains invalid value", 1);
                        this.errorMessage = "Security Level (" + string5 + ") contains invalid value";
                        this.errorCode = -2;
                        bl2 = true;
                    }
                    if (!bl2 && !this.inValidRange(string3, 0, -1)) {
                        this.getClass();
                        this.LogMsg(null, "processPubService :               >> Number of Arguments (" + string3 + ") must be greater than or equal to zero", 1);
                        this.errorMessage = "Number of Arguments (" + string3 + ") must be greater than or equal to zero";
                        this.errorCode = -2;
                        bl2 = true;
                    } else {
                        n = Integer.parseInt(string3);
                    }
                    if (!bl2 && !this.inValidRange(string8, 1, -1)) {
                        this.getClass();
                        this.LogMsg(null, "processPubService :               >> Number of Responses (" + string8 + ") must be greater than or equal to one", 1);
                        this.errorMessage = "Number of Responses (" + string8 + ") must be greater than or equal to one";
                        this.errorCode = -2;
                        bl2 = true;
                    } else {
                        n2 = Integer.parseInt(string8);
                    }
                    if (!bl2 && string9.length() > 0 && !this.isValidCommentCharacters(string9)) {
                        this.getClass();
                        this.LogMsg(null, "processPubService :               >> Service Description contains invalid characters", 1);
                        this.errorMessage = "Service Description contains invalid characters";
                        this.errorCode = -2;
                        bl2 = true;
                    }
                    if (!bl2 && string9.length() > 0 && string9.length() > 200) {
                        this.getClass();
                        this.LogMsg(null, "processPubService :               >> Service Description too long (needs to be less than 200 characters", 1);
                        this.errorMessage = "Service Description too long (needs to be less than 200 characters";
                        this.errorCode = -2;
                        bl2 = true;
                    }
                    if (!bl2) {
                        try {
                            string6 = "select teamID from Team where teamID=" + this.teamID + " and teamName='" + this.teamName + "';";
                            preparedStatement = this.getDBase().prepareStatement(string6);
                            preparedStatement.execute();
                            object = preparedStatement.getResultSet();
                            if (!((ResultSet)object).next()) {
                                this.getClass();
                                this.LogMsg(null, "processPubService :               >> Team '" + this.teamName + "' (ID : " + this.teamID + ") is not registered", 1);
                                this.errorMessage = "Team '" + this.teamName + "' (ID : " + this.teamID + ") is not registered";
                                this.errorCode = -4;
                                bl2 = true;
                            }
                            preparedStatement.close();
                        }
                        catch (SQLException var30_33) {
                            this.getClass();
                            this.LogMsg(null, "processPubService :                >> Error executing SQL=[" + string6 + "] - error=[" + var30_33.getMessage() + "]", 1);
                            this.errorMessage = "Error executing SQL=[" + string6 + "] - error=[" + var30_33.getMessage() + "]";
                            this.errorCode = -5;
                            bl2 = true;
                        }
                    }
                    if (bl2) continue;
                    try {
                        string6 = "select serviceID from Service where teamID=" + this.teamID + " and tagName='" + string4 + "';";
                        preparedStatement = this.getDBase().prepareStatement(string6);
                        preparedStatement.execute();
                        object = preparedStatement.getResultSet();
                        if (((ResultSet)object).next()) {
                            this.getClass();
                            this.LogMsg(null, "processPubService :               >> Team '" + this.teamName + "' (ID : " + this.teamID + ") has already published service " + string4, 1);
                            this.errorMessage = "Team '" + this.teamName + "' (ID : " + this.teamID + ") has already published service " + string4;
                            this.errorCode = -4;
                            bl2 = true;
                        } else {
                            string6 = "insert into Service(teamID, tagName, serviceName, securityLevel, description, ipAddress, port) VALUES(" + this.teamID + ",'" + string4 + "','" + string2 + "'," + string5 + ",'" + string9 + "','','');";
                            preparedStatement2 = this.getDBase().prepareStatement(string6);
                            preparedStatement2.execute();
                            this.getClass();
                            this.LogMsg(null, "processPubService :                >> Inserted service <" + string4 + "> into DBase for teamID " + this.teamID, 0);
                            preparedStatement2.close();
                            string6 = "select serviceID from Service where teamID=" + this.teamID + " and tagName='" + string4 + "';";
                            preparedStatement = this.getDBase().prepareStatement(string6);
                            preparedStatement.execute();
                            object = preparedStatement.getResultSet();
                            if (!((ResultSet)object).next()) {
                                this.getClass();
                                this.LogMsg(null, "processPubService :                >> No service found with name <" + string4 + "> for teamID " + this.teamID + " - Insert FAILED", 0);
                            } else {
                                this.getClass();
                                this.LogMsg(null, "processPubService :                >> Service <" + string4 + "> registered successfully", 0);
                                string = ((ResultSet)object).getString(1);
                            }
                        }
                        preparedStatement.close();
                    }
                    catch (SQLException var30_34) {
                        this.getClass();
                        this.LogMsg(null, "processPubService :                >> Error executing SQL=[" + string6 + "] - error=[" + var30_34.getMessage() + "]", 1);
                        this.errorMessage = "Error executing SQL=[" + string6 + "] - error=[" + var30_34.getMessage() + "]";
                        this.errorCode = -5;
                        bl2 = true;
                    }
                    continue;
                }
                this.getClass();
                this.LogMsg(null, "processPubService :                 >> SRV segment not according to Spec.", 1);
                if (string4.length() == 0) {
                    this.getClass();
                    this.LogMsg(null, "processPubService :                   >> Service tagName is blank.", 1);
                    if (this.errorMessage.length() == 0) {
                        this.errorMessage = "SRV segment not according to Spec (service tagName is BLANK).";
                        this.errorCode = -2;
                    }
                }
                if (string2.length() == 0) {
                    this.getClass();
                    this.LogMsg(null, "processPubService :                   >> Service name is blank.", 1);
                    if (this.errorMessage.length() == 0) {
                        this.errorMessage = "SRV segment not according to Spec (service name is BLANK).";
                        this.errorCode = -2;
                    }
                }
                if (string5.length() == 0) {
                    this.getClass();
                    this.LogMsg(null, "processPubService :                   >> Security Level is blank.", 1);
                    if (this.errorMessage.length() == 0) {
                        this.errorMessage = "SRV segment not according to Spec (security level is BLANK).";
                        this.errorCode = -2;
                    }
                }
                if (string3.length() == 0) {
                    this.getClass();
                    this.LogMsg(null, "processPubService :                   >> Number of Arguments is blank.", 1);
                    if (this.errorMessage.length() == 0) {
                        this.errorMessage = "SRV segment not according to Spec (numArgs is BLANK).";
                        this.errorCode = -2;
                    }
                }
                if (string8.length() != 0) continue;
                this.getClass();
                this.LogMsg(null, "processPubService :                   >> Number of Service Responses is blank.", 1);
                if (this.errorMessage.length() != 0) continue;
                this.errorMessage = "SRV segment not according to Spec (numResps is BLANK).";
                this.errorCode = -2;
                continue;
            }
            if (string7.compareTo("ARG|") == 0) {
                ++n3;
                string4 = "";
                this.getClass();
                n9 = this.currSegment.indexOf("|", n11 + 4);
                if (n9 >= 0) {
                    string4 = this.currSegment.substring(n11 + 4, n9);
                }
                if (string4.length() > 0) {
                    ++n10;
                }
                string2 = "";
                n11 = n9 + 1;
                this.getClass();
                n9 = this.currSegment.indexOf("|", n11);
                if (n9 >= 0) {
                    string2 = this.currSegment.substring(n11, n9);
                }
                if (string2.length() > 0) {
                    ++n10;
                }
                string5 = "";
                n11 = n9 + 1;
                this.getClass();
                n9 = this.currSegment.indexOf("|", n11);
                if (n9 >= 0) {
                    string5 = this.currSegment.substring(n11, n9);
                }
                if (string5.length() > 0) {
                    ++n10;
                }
                string3 = "";
                n11 = n9 + 1;
                this.getClass();
                n9 = this.currSegment.indexOf("|", n11);
                if (n9 >= 0) {
                    string3 = this.currSegment.substring(n11, n9);
                }
                if (string3.length() > 0) {
                    ++n10;
                }
                if (n10 == 4) {
                    bl2 = false;
                    string6 = "";
                    if (!this.inValidRange(string4, n5, n5)) {
                        this.getClass();
                        this.LogMsg(null, "processPubService :               >> ArgPosition (" + string4 + ") is not valid", 1);
                        this.errorMessage = "ARG Segment #" + n3 + " - ArgPosition (" + string4 + ") is not valid - expected " + n5;
                        this.errorCode = -2;
                        bl2 = true;
                    } else {
                        n5 = Integer.parseInt(string4) + 1;
                    }
                    if (!bl2 && !this.isAlphaNumeric(string2)) {
                        this.getClass();
                        this.LogMsg(null, "processPubService :               >> ArgName (" + string2 + ") contains invalid characters", 1);
                        this.errorMessage = "ARG Segment #" + n3 + " - ArgName (" + string2 + ") contains invalid characters";
                        this.errorCode = -2;
                        bl2 = true;
                    }
                    if (!bl2 && !this.isValidDatatype(string5)) {
                        this.getClass();
                        this.LogMsg(null, "processPubService :               >> ArgDatatype (" + string5 + ") is not valid", 1);
                        this.errorMessage = "ARG Segment #" + n3 + " - ArgDatatype (" + string5 + ") is not valid";
                        this.errorCode = -2;
                        bl2 = true;
                    }
                    if (!bl2 && !this.isValidArgtype(string3)) {
                        this.getClass();
                        this.LogMsg(null, "processPubService :               >> ArgMandatoryOptional (" + string3 + ") is not valid", 1);
                        this.errorMessage = "ARG Segment #" + n3 + " - ArgMandatoryOptional (" + string3 + ") is not valid";
                        this.errorCode = -2;
                        bl2 = true;
                    } else {
                        if (string3.compareToIgnoreCase("mandatory") == 0) {
                            string3 = "NO";
                        }
                        if (string3.compareToIgnoreCase("optional") == 0) {
                            string3 = "YES";
                        }
                    }
                    if (bl2) continue;
                    try {
                        string6 = "insert into Argument(serviceID, argName, argDatatype, argPosition, argOptional) VALUES(" + string + ",'" + string2 + "','" + string5.toUpperCase() + "'," + string4 + ",'" + string3 + "');";
                        preparedStatement2 = this.getDBase().prepareStatement(string6);
                        preparedStatement2.execute();
                        this.getClass();
                        this.LogMsg(null, "processPubService :                >> Inserted argument #" + n3 + " into DBase for serviceID " + string, 0);
                        preparedStatement2.close();
                    }
                    catch (SQLException var29_30) {
                        this.getClass();
                        this.LogMsg(null, "processPubService :                >> Error executing SQL=[" + string6 + "] - error=[" + var29_30.getMessage() + "]", 1);
                        this.errorMessage = "Error executing SQL=[" + string6 + "] - error=[" + var29_30.getMessage() + "]";
                        this.errorCode = -5;
                        bl2 = true;
                    }
                    continue;
                }
                this.getClass();
                this.LogMsg(null, "processPubService :                 >> ARG segment (#" + n3 + ") not according to Spec.", 1);
                if (string4.length() == 0) {
                    this.getClass();
                    this.LogMsg(null, "processPubService :                   >> Argument Position is blank.", 1);
                    if (this.errorMessage.length() == 0) {
                        this.errorMessage = "ARG segment (#" + n3 + ") not according to Spec (argPosition is BLANK).";
                        this.errorCode = -2;
                    }
                }
                if (string2.length() == 0) {
                    this.getClass();
                    this.LogMsg(null, "processPubService :                   >> Argument Name is blank.", 1);
                    if (this.errorMessage.length() == 0) {
                        this.errorMessage = "ARG segment (#" + n3 + ") not according to Spec (argName is BLANK).";
                        this.errorCode = -2;
                    }
                }
                if (string5.length() == 0) {
                    this.getClass();
                    this.LogMsg(null, "processPubService :                   >> Argument DataType is blank.", 1);
                    if (this.errorMessage.length() == 0) {
                        this.errorMessage = "ARG segment (#" + n3 + ") not according to Spec (argDatatype is BLANK).";
                        this.errorCode = -2;
                    }
                }
                if (string3.length() != 0) continue;
                this.getClass();
                this.LogMsg(null, "processPubService :                   >> Argument 'mandatoriness' is blank.", 1);
                if (this.errorMessage.length() != 0) continue;
                this.errorMessage = "ARG segment (#" + n3 + ") not according to Spec (is the argument mandatory or optional).";
                this.errorCode = -2;
                continue;
            }
            if (string7.compareTo("RSP|") == 0) {
                ++n4;
                string4 = "";
                this.getClass();
                n9 = this.currSegment.indexOf("|", n11 + 4);
                if (n9 >= 0) {
                    string4 = this.currSegment.substring(n11 + 4, n9);
                }
                if (string4.length() > 0) {
                    ++n10;
                }
                string2 = "";
                n11 = n9 + 1;
                this.getClass();
                n9 = this.currSegment.indexOf("|", n11);
                if (n9 >= 0) {
                    string2 = this.currSegment.substring(n11, n9);
                }
                if (string2.length() > 0) {
                    ++n10;
                }
                string5 = "";
                n11 = n9 + 1;
                this.getClass();
                n9 = this.currSegment.indexOf("|", n11);
                if (n9 >= 0) {
                    string5 = this.currSegment.substring(n11, n9);
                }
                if (string5.length() > 0) {
                    ++n10;
                }
                string3 = "";
                n11 = n9 + 1;
                this.getClass();
                n9 = this.currSegment.indexOf("|", n11);
                if (n9 >= 0) {
                    string3 = this.currSegment.substring(n11, n9);
                }
                if (string3.length() == 0) {
                    ++n10;
                }
                if (n10 == 4) {
                    bl2 = false;
                    string6 = "";
                    if (!this.inValidRange(string4, n6, n6)) {
                        this.getClass();
                        this.LogMsg(null, "processPubService :               >> RespPosition (" + string4 + ") is not valid", 1);
                        this.errorMessage = "RSP Segment #" + n4 + " - RespPosition (" + string4 + ") is not valid - expected " + n6;
                        this.errorCode = -2;
                        bl2 = true;
                    } else {
                        n6 = Integer.parseInt(string4) + 1;
                    }
                    if (!bl2 && !this.isAlphaNumeric(string2)) {
                        this.getClass();
                        this.LogMsg(null, "processPubService :               >> RespName (" + string2 + ") contains invalid characters", 1);
                        this.errorMessage = "RSP Segment #" + n4 + " - RespName (" + string2 + ") contains invalid characters";
                        this.errorCode = -2;
                        bl2 = true;
                    }
                    if (!bl2 && !this.isValidDatatype(string5)) {
                        this.getClass();
                        this.LogMsg(null, "processPubService :               >> RespDatatype (" + string5 + ") is not valid", 1);
                        this.errorMessage = "RSP Segment #" + n4 + " - RespDatatype (" + string5 + ") is not valid";
                        this.errorCode = -2;
                        bl2 = true;
                    }
                    if (bl2) continue;
                    try {
                        string6 = "insert into Response(serviceID, rspName, rspDatatype, rspPosition) VALUES(" + string + ",'" + string2 + "','" + string5.toUpperCase() + "'," + string4 + ");";
                        preparedStatement2 = this.getDBase().prepareStatement(string6);
                        preparedStatement2.execute();
                        this.getClass();
                        this.LogMsg(null, "processPubService :                >> Inserted response #" + n4 + " into DBase for serviceID " + string, 0);
                        preparedStatement2.close();
                    }
                    catch (SQLException var29_31) {
                        this.getClass();
                        this.LogMsg(null, "processPubService :                >> Error executing SQL=[" + string6 + "] - error=[" + var29_31.getMessage() + "]", 1);
                        this.errorMessage = "Error executing SQL=[" + string6 + "] - error=[" + var29_31.getMessage() + "]";
                        this.errorCode = -5;
                        bl2 = true;
                    }
                    continue;
                }
                this.getClass();
                this.LogMsg(null, "processPubService :                 >> RSP segment (#" + n4 + ") not according to Spec.", 1);
                if (string4.length() == 0) {
                    this.getClass();
                    this.LogMsg(null, "processPubService :                   >> Response Position is blank.", 1);
                    if (this.errorMessage.length() == 0) {
                        this.errorMessage = "RSP segment (#" + n4 + ") not according to Spec (rspPosition is BLANK).";
                        this.errorCode = -2;
                    }
                }
                if (string2.length() == 0) {
                    this.getClass();
                    this.LogMsg(null, "processPubService :                   >> Response Name is blank.", 1);
                    if (this.errorMessage.length() == 0) {
                        this.errorMessage = "RSP segment (#" + n4 + ") not according to Spec (rspName is BLANK).";
                        this.errorCode = -2;
                    }
                }
                if (string5.length() == 0) {
                    this.getClass();
                    this.LogMsg(null, "processPubService :                   >> Response DataType is blank.", 1);
                    if (this.errorMessage.length() == 0) {
                        this.errorMessage = "RSP segment (#" + n4 + ") not according to Spec (rspDatatype is BLANK).";
                        this.errorCode = -2;
                    }
                }
                if (string3.length() <= 0) continue;
                this.getClass();
                this.LogMsg(null, "processPubService :                   >> Last response field must be blank.", 1);
                if (this.errorMessage.length() != 0) continue;
                this.errorMessage = "RSP segment (#" + n4 + ") not according to Spec (last response field must be blank).";
                this.errorCode = -2;
                continue;
            }
            if (string7.compareTo("MCH|") == 0) {
                if (n != n3 && n > 0 || n2 != n4 && n2 > 0) {
                    this.getClass();
                    this.LogMsg(null, "processPubService :                 >> ARG/RSP segment(s) not consistent with SRV segment.", 1);
                    if (n != n3) {
                        this.getClass();
                        this.LogMsg(null, "processPubService :                   >> SRV said " + n + " ARG segments - found " + n3 + " such segments.", 1);
                        if (this.errorMessage.length() == 0) {
                            this.errorMessage = "Inconsistent ARG segments - SRV said " + n + " ARG segments - found " + n3 + " such segments..";
                            this.errorCode = -2;
                        }
                    }
                    if (n2 == n4) continue;
                    this.getClass();
                    this.LogMsg(null, "processPubService :                   >> SRV said " + n2 + " RSP segments - found " + n4 + " such segments.", 1);
                    if (this.errorMessage.length() != 0) continue;
                    this.errorMessage = "Inconsistent RSP segments - SRV said " + n2 + " RSP segments - found " + n4 + " such segments..";
                    this.errorCode = -2;
                    continue;
                }
                string4 = "";
                this.getClass();
                n9 = this.currSegment.indexOf("|", n11 + 4);
                if (n9 >= 0) {
                    string4 = this.currSegment.substring(n11 + 4, n9);
                }
                if (string4.length() > 0) {
                    ++n10;
                }
                string2 = "";
                n11 = n9 + 1;
                this.getClass();
                n9 = this.currSegment.indexOf("|", n11);
                if (n9 >= 0) {
                    string2 = this.currSegment.substring(n11, n9);
                }
                if (string2.length() > 0) {
                    ++n10;
                }
                if (n10 == 2) {
                    bl2 = false;
                    object = "";
                    if (!this.isValidIPFormat(string4)) {
                        this.getClass();
                        this.LogMsg(null, "processPubService :               >> Service IP Address (" + string4 + ") is not valid format", 1);
                        this.errorMessage = "MCH segment - Service IP Address (" + string4 + ")is not valid format.";
                        this.errorCode = -2;
                        bl2 = true;
                    }
                    if (!bl2 && !this.isValidIPAddress(string4)) {
                        if (this.errOnUnreachIP) {
                            this.getClass();
                            this.LogMsg(null, "processPubService :               >> Service IP Address (" + string4 + ") is not reachable", 1);
                            this.errorMessage = "MCH segment - Service IP Address (" + string4 + ") is not reachable.";
                            this.errorCode = -2;
                            bl2 = true;
                        } else {
                            this.getClass();
                            this.LogMsg(null, "processPubService :               >> Service IP Address (" + string4 + ") is not reachable", 1);
                        }
                    }
                    if (!bl2 && !this.inValidRange(string2, 2000, -1)) {
                        this.getClass();
                        this.LogMsg(null, "processPubService :               >> Service Port (" + string2 + ") not in valid range", 1);
                        this.errorMessage = "MCH Segment - Service Port (" + string2 + ") not in valid range";
                        this.errorCode = -2;
                        bl2 = true;
                    }
                    if (!bl2 && !this.isValidPublishLocation(string4, string2)) {
                        if (this.errOnUnconnectedPort) {
                            this.getClass();
                            this.LogMsg(null, "processPubService :               >> Publish Location (" + string4 + ", port " + string2 + ") is not accepting connections", 1);
                            this.errorMessage = "MCH segment - Publish Location (" + string4 + ", port " + string2 + ") is not accepting connections.";
                            this.errorCode = -2;
                            bl2 = true;
                        } else {
                            this.getClass();
                            this.LogMsg(null, "processPubService :               >> Publish Location (" + string4 + ", port " + string2 + ") is not accepting connections", 1);
                        }
                    }
                    if (bl2) continue;
                    try {
                        object = "update Service set ipaddress='" + string4 + "', port='" + string2 + "' where serviceID=" + string + ";";
                        preparedStatement = this.getDBase().prepareStatement((String)object);
                        preparedStatement.execute();
                        int n12 = preparedStatement.getUpdateCount();
                        if (n12 == 1) {
                            this.getClass();
                            this.LogMsg(null, "processPubService :                >> Updated service in DBase for serviceID " + string, 0);
                        } else {
                            this.getClass();
                            this.LogMsg(null, "processPubService :                >> Failed to update service in DBase for serviceID " + string, 0);
                        }
                        preparedStatement.close();
                    }
                    catch (SQLException var28_28) {
                        this.getClass();
                        this.LogMsg(null, "processPubService :                >> Error executing SQL=[" + (String)object + "] - error=[" + var28_28.getMessage() + "]", 1);
                        this.errorMessage = "Error executing SQL=[" + (String)object + "] - error=[" + var28_28.getMessage() + "]";
                        this.errorCode = -5;
                        bl2 = true;
                    }
                    continue;
                }
                this.getClass();
                this.LogMsg(null, "processPubService :                 >> MCH segment not according to Spec.", 1);
                if (string4.length() == 0) {
                    this.getClass();
                    this.LogMsg(null, "processPubService :                   >> Server's IP Address is blank.", 1);
                    if (this.errorMessage.length() == 0) {
                        this.errorMessage = "MSG segment not according to Spec (serverIPAddr is BLANK).";
                        this.errorCode = -2;
                    }
                }
                if (string2.length() != 0) continue;
                this.getClass();
                this.LogMsg(null, "processPubService :                   >> Server's Listening Port is blank.", 1);
                if (this.errorMessage.length() != 0) continue;
                this.errorMessage = "MCH segment not according to Spec (serverPort is BLANK).";
                this.errorCode = -2;
                continue;
            }
            this.getClass();
            this.LogMsg(null, "processPubService :                 >> Invalid segment directive found (" + string7 + ")", 1);
            if (this.errorMessage.length() != 0) continue;
            this.errorMessage = "Invalid segment directive found (" + string7 + ")";
            this.errorCode = -1;
        }
        if (this.errorCode == 0) {
            this.okContent = "|||";
            this.okBody = "";
        }
        return this.errorCode;
    }

    private int ProcessQueryService() {
        int n;
        boolean bl = false;
        int n2 = 0;
        int n3 = 1;
        int n4 = 0;
        int n5 = 0;
        n = 0;
        String string = "";
        String string2 = "";
        String string3 = "";
        String string4 = "";
        String string5 = "";
        String string6 = "";
        String string7 = "";
        String string8 = "optional";
        char[] arrc = new char[]{this.endSegmentChar};
        int n6 = 0;
        String string9 = this.currSegment.substring(0, 4);
        this.getClass();
        this.LogMsg(null, "processQueryService :             >> Parsing Segment <" + string9.toUpperCase() + ">", 1);
        if (string9.compareTo("SRV|") == 0) {
            String string10 = "";
            this.getClass();
            int n7 = this.currSegment.indexOf("|", n6 + 4);
            if (n7 >= 0) {
                string10 = this.currSegment.substring(n6 + 4, n7);
            }
            n6 = n7 + 1;
            String string11 = this.currSegment.substring(n6);
            if (string10.length() > 0 && string11.compareTo("|||||") == 0) {
                ResultSet resultSet;
                PreparedStatement preparedStatement;
                boolean bl2 = false;
                String string12 = "";
                try {
                    string12 = "select teamID from Team where teamName='" + this.teamName + "' and teamID=" + this.teamID + ";";
                    preparedStatement = this.getDBase().prepareStatement(string12);
                    preparedStatement.execute();
                    resultSet = preparedStatement.getResultSet();
                    if (!resultSet.next()) {
                        this.getClass();
                        this.LogMsg(null, "processQueryService :               >> Team '" + this.teamName + "' (ID : " + this.teamID + ") is not registered in DBase", 1);
                        this.errorMessage = "Team '" + this.teamName + "' (ID : " + this.teamID + ") is not registered in DBase";
                        this.errorCode = -4;
                        bl2 = true;
                    }
                    preparedStatement.close();
                }
                catch (SQLException var31_25) {
                    this.getClass();
                    this.LogMsg(null, "processQueryService :                >> Error executing SQL=[" + string12 + "] - error=[" + var31_25.getMessage() + "]", 1);
                    this.errorMessage = "Error executing SQL=[" + string12 + "] - error=[" + var31_25.getMessage() + "]";
                    this.errorCode = -5;
                    bl2 = true;
                }
                if (!bl2 && !this.isValidTagName(string10)) {
                    this.getClass();
                    this.LogMsg(null, "processQueryService :               >> Tagname (" + string10 + ") is not valid", 1);
                    this.errorMessage = "Tagname (" + string10 + ") is not valid";
                    this.errorCode = -2;
                    bl2 = true;
                }
                if (!bl2) {
                    try {
                        string12 = "select count(*) from Service where tagName='" + string10 + "';";
                        if (!this.teamTestOwn) {
                            string12 = "select count(*) from Service where tagName='" + string10 + "' and teamID!=" + this.teamID + ";";
                        }
                        preparedStatement = this.getDBase().prepareStatement(string12);
                        preparedStatement.execute();
                        resultSet = preparedStatement.getResultSet();
                        if (!resultSet.next()) {
                            this.getClass();
                            this.LogMsg(null, "processQueryService :               >> No <" + string10 + "> services exist in the DBase", 1);
                            this.errorMessage = "No <" + string10 + "> services exist in the DBase";
                            this.errorCode = -4;
                            bl2 = true;
                        } else {
                            n2 = Integer.parseInt(resultSet.getString(1));
                            if (n2 == 0) {
                                this.getClass();
                                this.LogMsg(null, "processQueryService :               >> No <" + string10 + "> services exist in the DBase", 1);
                                this.errorMessage = "No <" + string10 + "> services exist in the DBase";
                                this.errorCode = -4;
                                bl2 = true;
                            } else {
                                n3 = 1 + (int)(Math.random() * (double)(n2 - 1 + 1));
                            }
                        }
                        preparedStatement.close();
                    }
                    catch (SQLException var31_26) {
                        this.getClass();
                        this.LogMsg(null, "processQueryService :                >> Error executing SQL=[" + string12 + "] - error=[" + var31_26.getMessage() + "]", 1);
                        this.errorMessage = "Error executing SQL=[" + string12 + "] - error=[" + var31_26.getMessage() + "]";
                        this.errorCode = -5;
                        bl2 = true;
                    }
                }
                if (!bl2) {
                    try {
                        string12 = "select s.serviceID, s.teamID, s.description, s.ipAddress, s.port, s.serviceName, t.teamName from Service s, Team t where s.tagName='" + string10 + "' and s.teamID=t.teamID;";
                        if (!this.teamTestOwn) {
                            string12 = "select  s.serviceID, s.teamID, s.description, s.ipAddress, s.port, s.serviceName, t.teamName from Service s, Team t where s.tagName='" + string10 + "' and s.teamID!=" + this.teamID + " and s.teamID=t.teamID;";
                        }
                        preparedStatement = this.getDBase().prepareStatement(string12);
                        preparedStatement.execute();
                        resultSet = preparedStatement.getResultSet();
                        if (!resultSet.next()) {
                            String string13 = "";
                            if (n3 == 1) {
                                string13 = "1st";
                            }
                            if (n3 == 2) {
                                string13 = "2nd";
                            }
                            if (n3 == 3) {
                                string13 = "3rd";
                            }
                            if (n3 > 3) {
                                string13 = "" + n3 + "th";
                            }
                            this.getClass();
                            this.LogMsg(null, "processQueryService :               >> An error occurred while selecting the " + string13 + " <" + string10 + "> service in the DBase", 1);
                            this.errorMessage = "An error occurred while selecting the " + string13 + " <" + string10 + "> service in the DBase";
                            this.errorCode = -4;
                            bl2 = true;
                        }
                        if (!bl2) {
                            if (n3 > 1) {
                                for (int i = 1; i < n3; ++i) {
                                    resultSet.next();
                                }
                            }
                            string = resultSet.getString(1);
                            string2 = resultSet.getString(2);
                            string4 = resultSet.getString(3);
                            string5 = resultSet.getString(4);
                            string6 = resultSet.getString(5);
                            string7 = resultSet.getString(6);
                            string3 = resultSet.getString(7);
                            preparedStatement.close();
                            string12 = "select count(*) from Argument where serviceID=" + string + ";";
                            preparedStatement = this.getDBase().prepareStatement(string12);
                            preparedStatement.execute();
                            resultSet = preparedStatement.getResultSet();
                            if (!resultSet.next()) {
                                this.getClass();
                                this.LogMsg(null, "processQueryService :               >> An error occurred while retrieving the number of arguments for <" + string10 + "> service (serviceID=" + string + ") in the DBase", 1);
                                this.errorMessage = "An error occurred while retrieving the number of arguments for <" + string10 + "> service (serviceID=" + string + ") in the DBase";
                                this.errorCode = -4;
                                bl2 = true;
                            } else {
                                n4 = Integer.parseInt(resultSet.getString(1));
                                preparedStatement.close();
                                string12 = "select count(*) from Response where serviceID=" + string + ";";
                                preparedStatement = this.getDBase().prepareStatement(string12);
                                preparedStatement.execute();
                                resultSet = preparedStatement.getResultSet();
                                if (!resultSet.next()) {
                                    this.getClass();
                                    this.LogMsg(null, "processQueryService :               >> An error occurred while retrieving the number of responses for <" + string10 + "> service (serviceID=" + string + ") in the DBase", 1);
                                    this.errorMessage = "An error occurred while retrieving the number of responses for <" + string10 + "> service (serviceID=" + string + ") in the DBase";
                                    this.errorCode = -4;
                                    bl2 = true;
                                } else {
                                    n5 = Integer.parseInt(resultSet.getString(1));
                                    n = 2 + n4 + n5;
                                }
                            }
                        }
                        preparedStatement.close();
                    }
                    catch (SQLException var31_29) {
                        this.getClass();
                        this.LogMsg(null, "processQueryService :                >> Error executing SQL=[" + string12 + "] - error=[" + var31_29.getMessage() + "]", 1);
                        this.errorMessage = "Error executing SQL=[" + string12 + "] - error=[" + var31_29.getMessage() + "]";
                        this.errorCode = -5;
                        bl2 = true;
                    }
                }
                if (!bl2) {
                    this.okBody = "SRV|" + string3 + "|" + string7 + "||" + n4 + "|" + n5 + "|" + string4 + "|" + new String(arrc);
                    try {
                        string12 = "select argPosition, argName, argDatatype, argOptional from Argument where serviceID='" + string + "' order by argPosition;";
                        preparedStatement = this.getDBase().prepareStatement(string12);
                        preparedStatement.execute();
                        resultSet = preparedStatement.getResultSet();
                        if (!resultSet.next()) {
                            this.getClass();
                            this.LogMsg(null, "processQueryService :               >> An error occurred while retrieving the arguments for <" + string10 + "> service (serviceID=" + string + ") in the DBase", 1);
                            this.errorMessage = "An error occurred while retrieving the arguments for <" + string10 + "> service (serviceID=" + string + ") in the DBase";
                            this.errorCode = -4;
                            bl2 = true;
                        } else {
                            do {
                                string8 = "optional";
                                if (resultSet.getString(4).compareTo("NO") == 0) {
                                    string8 = "mandatory";
                                }
                                this.okBody = this.okBody + "ARG|" + resultSet.getString(1) + "|" + resultSet.getString(2) + "|" + resultSet.getString(3) + "|" + string8 + "||" + new String(arrc);
                            } while (resultSet.next());
                        }
                        if (!bl2) {
                            preparedStatement.close();
                            string12 = "select rspPosition, rspName, rspDatatype from Response where serviceID='" + string + "' order by rspPosition;";
                            preparedStatement = this.getDBase().prepareStatement(string12);
                            preparedStatement.execute();
                            resultSet = preparedStatement.getResultSet();
                            if (!resultSet.next()) {
                                this.getClass();
                                this.LogMsg(null, "processQueryService :               >> An error occurred while retrieving the responses for <" + string10 + "> service (serviceID=" + string + ") in the DBase", 1);
                                this.errorMessage = "An error occurred while retrieving the responses for <" + string10 + "> service (serviceID=" + string + ") in the DBase";
                                this.errorCode = -4;
                                bl2 = true;
                            } else {
                                do {
                                    this.okBody = this.okBody + "RSP|" + resultSet.getString(1) + "|" + resultSet.getString(2) + "|" + resultSet.getString(3) + "||" + new String(arrc);
                                } while (resultSet.next());
                            }
                        }
                        preparedStatement.close();
                        if (!bl2) {
                            this.okBody = this.okBody + "MCH|" + string5 + "|" + string6 + "|" + new String(arrc);
                        }
                    }
                    catch (SQLException var31_30) {
                        this.getClass();
                        this.LogMsg(null, "processQueryService :                >> Error executing SQL=[" + string12 + "] - error=[" + var31_30.getMessage() + "]", 1);
                        this.errorMessage = "Error executing SQL=[" + string12 + "] - error=[" + var31_30.getMessage() + "]";
                        this.errorCode = -5;
                        bl2 = true;
                    }
                }
            } else {
                this.getClass();
                this.LogMsg(null, "processQueryService :                 >> SRV segment not according to Spec.", 1);
                if (string10.length() == 0) {
                    this.getClass();
                    this.LogMsg(null, "processQueryService :                   >> Calling tagName is blank.", 1);
                    if (this.errorMessage.length() == 0) {
                        this.errorMessage = "SRV segment not according to Spec (calling tagName is BLANK).";
                        this.errorCode = -2;
                    }
                }
                if (string11.compareTo("|||||") != 0) {
                    this.getClass();
                    this.LogMsg(null, "processQueryService :                   >> All fields after <tagName> must be blank.", 1);
                    if (this.errorMessage.length() == 0) {
                        this.errorMessage = "SRV segment not according to Spec (All fields after <tagName> must be BLANK).";
                        this.errorCode = -2;
                    }
                }
            }
        } else {
            this.getClass();
            this.LogMsg(null, "processQueryService :                 >> Failed to find SRV directive in second segment", 1);
            if (this.errorMessage.length() == 0) {
                this.errorMessage = "SRV directive not in second message segment";
                this.errorCode = -1;
            }
        }
        if (this.errorCode == 0) {
            this.okContent = "||" + n + "|";
        }
        return this.errorCode;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void LogMsg(Exception exception, String string, int n) {
        BufferedWriter bufferedWriter = null;
        String string2 = this.soaListenerProperties.getProperty("SOAListenerLogLevel", "INFO");
        this.getClass();
        int n2 = 1;
        if (string2.compareTo("DEBUG") == 0) {
            this.getClass();
            n2 = 0;
        }
        if (n >= n2) {
            File file = new File(this.soaListenerProperties.getProperty("SOAListenerLogDir") + File.separator + "SOARegisterListener.log");
            try {
                bufferedWriter = new BufferedWriter(new FileWriter(file, true));
                if (exception != null) {
                    bufferedWriter.write(this.FORMAT_TIMESTAMP.format(new Date(System.currentTimeMillis())) + " " + string + " " + exception);
                } else {
                    bufferedWriter.write(this.FORMAT_TIMESTAMP.format(new Date(System.currentTimeMillis())) + " " + string);
                }
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
            catch (IOException var8_9) {
                System.out.println("(SOARegisterListener) : Error while logging: " + var8_9);
            }
            finally {
                if (bufferedWriter != null) {
                    try {
                        bufferedWriter.close();
                    }
                    catch (IOException var8_10) {
                        System.out.println("(SOARegisterListener) : Error while closing logging file : " + var8_10);
                    }
                }
            }
        }
    }
}