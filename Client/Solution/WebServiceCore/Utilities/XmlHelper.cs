// --------------------------------------------------------------------------------------------------------------------
// <copyright file="XmlHelper.cs" company="none">
//    Copyright by Grigory Kozyrev
// </copyright>
// <summary>
//    Project: SOA_Assignment2
//    Author: Grigory Kozyrev
//    Date: 26/09/2016
//    Last Updated: 30/09/2016
// </summary>
// --------------------------------------------------------------------------------------------------------------------

#region Using

using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Xml;
using WebServiceCore.Models;

#endregion

namespace WebServiceCore.Utilities
{
    /// <summary>
    /// Helper class to read XML config document
    /// </summary>
    public static class XmlHelper
    {
        #region Constants

        private const string NEW_DATA_SET = "NewDataSet";
        private const string TABLE = "Table";
        private const string TYPE_STRING = "string";
        private const string ENVELOPE = "Envelope";

        #endregion

        #region External Members

        /// <summary>
        /// Reads config file and extracts web services from it.
        /// </summary>
        /// <param name="path">Path to the config file.</param>
        /// <returns><see cref="IEnumerable{T}"/> of <see cref="IWebService"/> objects for web services found in config file.</returns>
        public static IEnumerable<IWebService> ReadConfig(string path)
        {
            if (!File.Exists(path))
            {
                throw new FileLoadException($"Not able to find file from the path: [{path}]");
            }

            var xml = new XmlDocument();

            try
            {
                using (var stream = new FileStream(path, FileMode.Open, FileAccess.Read))
                {
                    xml.Load(stream);
                }
            }
            catch (Exception e)
            {
                Logger.Error("Failed to load XML", e);
                throw new FileLoadException($"Not able to load XML file from the path: [{path}]");
            }

            return ExtractServices(xml);
        }

        public static IEnumerable<ResponseHolder> ConvertSoapXml(string soap)
        {
            var doc = new XmlDocument();
            doc.LoadXml(soap);

            var dataSet = new List<ResponseHolder>();

            int i = 1;
            foreach (XmlNode node in doc.ChildNodes)
            {
                if (node == null ||
                    !node.Name.Contains(ENVELOPE))
                {
                    continue;
                }

                dataSet = dataSet.Concat(UnwrapData(node.FirstChild
                        ?.FirstChild
                        ?.FirstChild,
                    ref i)).ToList();
            }

            return dataSet;
        }

        #endregion

        #region Private Members

        private static IEnumerable<ResponseHolder> UnwrapData(XmlNode dataNode, ref int count)
        {
            var dataSet = new List<ResponseHolder>();
            if (dataNode == null ||
                dataNode.FirstChild == null)
            {
                return dataSet;
            }

            if (dataNode.FirstChild.Name != NEW_DATA_SET)
            {
                dataSet.Add(new ResponseHolder(string.Format("Item {0}", count++), WrapParameters(dataNode)));
            }
            else
            {
                foreach (XmlNode tableNode in dataNode.FirstChild.ChildNodes)
                {
                    if (tableNode == null ||
                        tableNode.Name != TABLE)
                    {
                        continue;
                    }

                    dataSet.Add(new ResponseHolder(string.Format("Item {0}", count++), WrapParameters(tableNode)));
                }
            }

            return dataSet;
        }

        private static IEnumerable<IMethodParameter> WrapParameters(XmlNode node)
        {
            var elements = new List<IMethodParameter>();

            foreach (XmlNode elementNode in node.ChildNodes)
            {
                elements.Add(new MethodParameter(elementNode.Name, TYPE_STRING, elementNode.InnerText));
            }

            return elements;
        }

        private static IEnumerable<IWebService> ExtractServices(XmlDocument doc)
        {
            var services = new List<IWebService>();

            foreach (XmlNode node in doc.GetElementsByTagName("service"))
            {
                if (node == null)
                {
                    continue;
                }

                var name = GetAttribute(node, "name");
                var host = GetAttribute(node, "host");
                var url = GetAttribute(node, "serviceUrl");
                var methods = ExtractMethods(node) ?? new List<IWebMethod>();

                var names = services.Select(x => x.Name)
                    .Where(x => x.StartsWith(name))
                    .OrderByDescending(x => x).ToArray();
                if (names.Any())
                {
                    int number = 1;

                    if (!names[0].Equals(name) &&
                        !int.TryParse(names[0].Remove(0, name.Length), out number))
                    {
                        continue;
                    }

                    name += " " + (number + 1);
                }

                //services.Add(new WebService(name, host, url, methods));
            }

            return services;
        }

        private static IEnumerable<IWebMethod> ExtractMethods(XmlNode node)
        {
            var methods = new List<IWebMethod>();

            var methodsTree = node["methods"];
            if (methodsTree == null)
            {
                return null;
            }

            foreach (XmlNode methodNode in methodsTree.GetElementsByTagName("method"))
            {
                if (methodNode == null)
                {
                    continue;
                }

                var name = GetAttribute(methodNode, "name");
                var xmlns = GetAttribute(methodNode, "xmlns");
                var parameters = ExtractParameters(methodNode) ?? new List<IMethodParameter>();

                methods.Add(new WebMethod(name, xmlns, parameters));
            }

            return methods;
        }

        private static IEnumerable<IMethodParameter> ExtractParameters(XmlNode node)
        {
            var parameters = new List<IMethodParameter>();

            var parametersTree = node["parameters"];
            if (parametersTree == null)
            {
                return null;
            }

            foreach (XmlNode parameterNode in parametersTree.GetElementsByTagName("parameter"))
            {
                if (parameterNode == null)
                {
                    continue;
                }

                var name = GetAttribute(parameterNode, "name");
                var type = GetAttribute(parameterNode, "type");
                var value = GetAttribute(parameterNode, "value");
                var rule = GetAttribute(parameterNode, "rule");
                var help = GetAttribute(parameterNode, "help");

                parameters.Add(new MethodParameter(name, type, value, rule, help, false));
            }

            return parameters;
        }

        private static string GetAttribute(XmlNode node, string name)
        {
            if (node.Attributes == null)
            {
                return null;
            }

            return node.Attributes[name]?.Value;
        }

        #endregion
    }
}