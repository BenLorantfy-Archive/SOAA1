/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package knockknockclient;

/*
 * Decompiled with CFR 0_118.
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.swing.JOptionPane;

public class KnockKnockClient {
    private static final int START_MSG = 11;
    private static final int END_MSG = 28;
    private static final int END_SEGMENT = 13;

    public static void main(String[] arrstring) throws IOException {
        int n;
        Socket socket = null;
        PrintWriter printWriter = null;
        BufferedReader bufferedReader = null;
        String string = JOptionPane.showInputDialog(null, "Host to Talk to ?", "Specify the HOST", 1);
        String string2 = JOptionPane.showInputDialog(null, "What Port is it Listening on ?", "Specify the PORT", 1);
        try {
            n = Integer.parseInt(string2);
        }
        catch (Exception var7_7) {
            n = 3128;
        }
        char[] arrc = new char[]{'\u000b'};
        char[] arrc2 = new char[]{'\u001c'};
        char[] arrc3 = new char[]{'\r'};
        boolean bl = true;
        boolean bl2 = false;
        boolean bl3 = false;
        String string3 = "";
        String string4 = JOptionPane.showInputDialog(null, "Filename Containing HL7 Message", "Test Filename", 1);
        while (string4.length() > 0) {
            try {
                socket = new Socket(string, n);
                printWriter = new PrintWriter(socket.getOutputStream(), true);
                bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            }
            catch (UnknownHostException var23_24) {
                System.out.println("Don't know about host: " + string);
                System.exit(1);
            }
            catch (IOException var23_25) {
                System.out.println("Couldn't get I/O for the Connection to " + string + " on Port (" + string2 + ")");
                System.exit(1);
            }
            try {
                String string5;
                File file = new File("." + File.separator + string4);
                BufferedReader bufferedReader2 = new BufferedReader(new FileReader(file));
                long l = file.length();
                System.out.println("  >> Transmitting File : ." + File.separator + string4 + " (" + l + " bytes)");
                String string6 = new String(arrc);
                while ((string5 = bufferedReader2.readLine()) != null) {
                    l = l - (long)string5.length() - 1;
                    System.out.println("     >> Segment read from file (segLength=" + string5.length() + " bytes, " + l + " bytes remaining to send)");
                    if (bl) {
                        bl = false;
                    }
                    string6 = string6 + string5 + new String(arrc3);
                    if (l < 6) {
                        string6 = string6.indexOf("READY") >= 0 || string6.indexOf("BYE") >= 0 ? string6.substring(0, string6.length() - 1) : string6 + new String(arrc2);
                        bl2 = true;
                    }
                    if (!bl2) continue;
                }
                if (!bl2) {
                    string6 = string6 + new String(arrc2);
                    bl2 = true;
                }
                printWriter.println(string6);
                printWriter.flush();
                bufferedReader2.close();
                System.out.println("  >> Message sent ...");
                string3 = bufferedReader.readLine();
                if (arrc3[0] == '\r') {
                    bl3 = true;
                    if (string3.indexOf("SOA|OK|") >= 0) {
                        if (string3.indexOf("SOA|OK|||") >= 0) {
                            if (string3.indexOf("SOA|OK||||") >= 0) {
                                bl3 = false;
                            }
                        } else {
                            bl3 = false;
                        }
                    }
                    if (string3.indexOf("SOA|NOT-OK|") >= 0) {
                        bl3 = false;
                    }
                    string3 = string3.substring(1) + "\n";
                    while (bl3) {
                        int n2;
                        String string7 = bufferedReader.readLine();
                        if (string7.charAt(n2 = string7.length() - 1) == arrc2[0]) {
                            bl3 = false;
                            continue;
                        }
                        string3 = string3 + string7 + "\n";
                    }
                }
                System.out.println("  >> Server Responds : " + string3);
            }
            catch (Exception var23_26) {
                System.out.println("Exception in Transmitting Data : " + var23_26.getMessage());
                printWriter.close();
                bufferedReader.close();
                socket.close();
                System.exit(1);
            }
            printWriter.close();
            bufferedReader.close();
            socket.close();
            if (string3.indexOf("AE") >= 0) break;
            string4 = JOptionPane.showInputDialog(null, "Filename Containing HL7 Message", "Test Filename", 1);
            if (string4 == null) {
                System.exit(0);
            }
            bl = true;
            bl2 = false;
        }
        System.exit(0);
    }
}
