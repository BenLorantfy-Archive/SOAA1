using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Newtonsoft;
using Grapevine.Server;
using Grapevine.Server.Attributes;
using System.Threading;
using Grapevine.Shared;
using System.Net;
using Grapevine.Client;

namespace giorpTotaller
{
    class Program
    {
        static void Main(string[] args)
        {
            var server = new RestServer();
            server.Start();
            //defaults to port 1234
            while (server.IsListening)
            {
                Thread.Sleep(300);
            }

            // This little tidbit will keep your console open after the server
            // stops until you explicitly close it by pressing the enter key
            Console.WriteLine("Press Enter to Continue...");
            Console.ReadLine();
        }
        

        public sealed class MyResource : RestResource
        {
            //[RestRoute(HttpMethod = HttpMethod.GET, PathInfo = @"^/foo/\d+$")]
            //[RestRoute(HttpMethod = HttpMethod.POST, PathInfo = @"^/bar/\D+$")]
            //public void HandleFooBarRequests(HttpListenerContext context)
            //{
            //    this.SendTextResponse(context, "foo bar is a success!");
            //}

            [RestRoute]
            public void HandleAllGetRequests(HttpListenerContext context)
            {
                bool error = false;
                bool calcError = false;
                //this.SendTextResponse(context, "GET is a success!");                
                float subTotal = 0;
                float Gst = 0;
                float Pst = 0;
                float Hst = 0;
                float grandTotal = 0;
                float purchaseAmount = 0;
                string province = "";
                
                var jsonObj = Newtonsoft.Json.JsonConvert.DeserializeObject<Dictionary<string, object>>(context.Request.InputStream.ToString());
                province = jsonObj["Province"].ToString();
                string sPurchaseAmount = jsonObj["PurchaseAmount"].ToString();

                if (!float.TryParse(sPurchaseAmount, out purchaseAmount))
                {
                    error = true;
                }

                if (purchaseAmount < 0)
                {
                    error = true;
                }

                if (province.Length != 2)
                {
                    error = true;
                }

                if (!error)
                {
                    calcError = CalculateTotalPurchase(purchaseAmount, province, out subTotal, out Gst, out Pst, out Hst, out grandTotal);
                }

                context.Response.AddHeader("SubTotal", subTotal.ToString());
                context.Response.AddHeader("Gst", Gst.ToString());
                context.Response.AddHeader("Pst", Pst.ToString());
                context.Response.AddHeader("Hst", Hst.ToString());
                context.Response.AddHeader("GrandTotal", grandTotal.ToString());
            }

            private bool CalculateTotalPurchase(float purchaseAmount, string province, out float subTotal, out float Gst, out float Pst, out float Hst, out float grandTotal)
            {
                bool retValue = true;

                subTotal = purchaseAmount;
                Gst = Pst = Hst = 0;

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
                        retValue = false;
                        subTotal = 0;
                        break;
                }

                grandTotal = subTotal + Gst + Pst + Hst;

                return retValue;
            }
        }

    }
}
