//Names: Amshar Basheer, Grigory Kozyrev, Ben Lorantfy, Kyle Stevenson
//Project Name: giorpTotaller
//File Name: Program.cs
//Date: 2016-11-28
//Description: this file contains the code for the giorpTotaller service

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Newtonsoft;
using System.Threading;
using System.Net;
using NHttp;
using System.Diagnostics;
using System.IO;

namespace giorpTotaller
{
    class Program
    {
        static void Main(string[] args)
        {
            RunService();            
        }

        /// <summary>
        /// Starts server for giorpTotaller service
        /// </summary>
        private static void RunService()
        {
            using (var server = new HttpServer())
            {
                server.RequestReceived += (s, e) =>
                {
                    using (var writer = new StreamWriter(e.Response.OutputStream))
                    {
                        bool error = false;
                        bool calcError = false;
                        float subTotal = 0;
                        float Gst = 0;
                        float Pst = 0;
                        float Hst = 0;
                        float grandTotal = 0;
                        float purchaseAmount = 0;
                        string province = "";
                        string errorMsg = "";
                        int errorCode = 0;

                        if (e.Request.InputStream != null)
                        {
                            // convert request input stream to string
                            StreamReader reader = new StreamReader(e.Request.InputStream);
                            string text = reader.ReadToEnd();

                            //deserialize text into JSON and load parameters
                            var jsonObj = Newtonsoft.Json.JsonConvert.DeserializeObject<Dictionary<string, object>>(text);
                            province = jsonObj["Province"].ToString();
                            string sPurchaseAmount = jsonObj["PurchaseAmount"].ToString();

                            //error check parameters
                            if (!float.TryParse(sPurchaseAmount, out purchaseAmount))
                            {
                                error = true;
                                errorMsg += "invalid input for purchaseAmount ";
                                errorCode += 1;
                            }

                            if (purchaseAmount < 0)
                            {
                                error = true;
                                errorMsg += "purchaseAmount can't be negative ";
                                errorCode += 2;
                            }

                            if (province.Length != 2)
                            {
                                error = true;
                                errorMsg += "province length not equal to 2";
                                errorCode += 4;
                            }

                            //if no errors then call CalculateTotalPurchase to get results into variables
                            if (!error)
                            {
                                calcError = CalculateTotalPurchase(purchaseAmount, province, out subTotal, out Gst, out Pst, out Hst, out grandTotal);
                            }

                            //setup dictionary with the return values
                            Dictionary<string, object> responseValues = new Dictionary<string, object>();
                            responseValues.Add("SubTotal", subTotal.ToString("N2"));
                            responseValues.Add("Gst", Gst.ToString("N2"));
                            responseValues.Add("Pst", Pst.ToString("N2"));
                            responseValues.Add("Hst", Hst.ToString("N2"));
                            responseValues.Add("GrandTotal", grandTotal.ToString("N2"));

                            if (error || calcError)
                            {
                                responseValues.Add("Error", true);

                                if (error)
                                {
                                    responseValues.Add("Message", errorMsg);
                                }
                                else if (calcError)
                                {
                                    responseValues.Add("Message", "province code doesn't match a province or territory");
                                    errorCode += 8;
                                }
                                responseValues.Add("Code", errorCode.ToString());
                            }
                            else
                            {
                                responseValues.Add("Error", false);
                            }

                            //serialize into JSON, then turn into char array, then into byte array and write to response output stream
                            var jsonResponseObj = Newtonsoft.Json.JsonConvert.SerializeObject(responseValues);
                            var jsonByteArray = Encoding.GetEncoding("UTF-8").GetBytes(jsonResponseObj.ToCharArray());
                            e.Response.OutputStream.Write(jsonByteArray, 0, jsonByteArray.Length);
                        }

                    }
                };

                server.EndPoint = new IPEndPoint(IPAddress.Loopback, 1234);

                server.Start();

                Console.WriteLine("giorpTotaller service started on port 1234");
                Console.WriteLine("Press any key to continue...");
                Console.ReadKey();
            }
        }

        /// <summary>
        /// Takes in a purchaseAmount and province and calculates out variables subTotal, Gst, Pst, Hst, and grandTotal
        /// </summary>
        /// <param name="purchaseAmount">amount of purchase (equivalent of subTotal)</param>
        /// <param name="province">province/territory (2 letter region code)</param>
        /// <param name="subTotal">(out variable) same as purchase amount</param>
        /// <param name="Gst">(out variable) calculated Gst</param>
        /// <param name="Pst">(out variable) calculated Pst</param>
        /// <param name="Hst">(out variable) calculated Hst</param>
        /// <param name="grandTotal">(out variable) calculated grandTotal = subTotal plus taxes</param>
        /// <returns></returns>
        private static bool CalculateTotalPurchase(float purchaseAmount, string province, out float subTotal, out float Gst, out float Pst, out float Hst, out float grandTotal)
        {
            bool retValueError = false; //bool for error check

            subTotal = purchaseAmount;
            Gst = Pst = Hst = 0;
            //calculate taxes according to region
            switch (province)
            {
                case "NL":
                    Gst = 0;
                    Pst = 0;
                    Hst = 0.13f * subTotal;
                    break;
                case "NS":
                    Gst = 0;
                    Pst = 0;
                    Hst = 0.15f * subTotal;
                    break;
                case "NB":
                    Gst = 0;
                    Pst = 0;
                    Hst = 0.13f * subTotal;
                    break;
                case "PE":
                    Gst = 0.05f * subTotal;
                    Pst = 0.10f * (subTotal + Gst);
                    Hst = 0;
                    break;
                case "QC":
                    Gst = 0.05f * subTotal;
                    Pst = 0.095f * (subTotal + Gst);
                    Hst = 0;
                    break;
                case "ON":
                    Gst = 0;
                    Pst = 0;
                    Hst = 0.13f * subTotal;
                    break;
                case "MB":
                    Gst = 0.05f * subTotal;
                    Pst = 0.07f * subTotal;
                    Hst = 0;
                    break;
                case "SK":
                    Gst = 0.05f * subTotal;
                    Pst = 0.05f * subTotal;
                    Hst = 0;
                    break;
                case "AB":
                    Gst = 0.05f * subTotal;
                    Pst = 0;
                    Hst = 0;
                    break;
                case "BC":
                    Gst = 0;
                    Pst = 0;
                    Hst = 0.12f * subTotal;
                    break;
                case "YT":
                    Gst = 0.05f * subTotal;
                    Pst = 0;
                    Hst = 0;
                    break;
                case "NT":
                    Gst = 0.05f * subTotal;
                    Pst = 0;
                    Hst = 0;
                    break;
                case "NU":
                    Gst = 0.05f * subTotal;
                    Pst = 0;
                    Hst = 0;
                    break;
                default:
                    retValueError = true;
                    subTotal = 0;
                    break;
            }

            grandTotal = subTotal + Gst + Pst + Hst;

            return retValueError;
        }
        
    }
}
