// --------------------------------------------------------------------------------------------------------------------
// <copyright file="WebMethod.cs" company="none">
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

#endregion

namespace WebServiceCore.Models
{
    /// <summary>
    /// Standard implementation of web method
    /// </summary>
    /// <seealso cref="IWebMethod" />
    public class WebMethod : IWebMethod
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="WebMethod" /> class.
        /// </summary>
        /// <param name="name">The method name.</param>
        /// <param name="xmlns">The xmlns url of the method.</param>
        /// <param name="parameters">The method parameters.</param>
        public WebMethod(string name, string xmlns, IEnumerable<IMethodParameter> parameters)
        {
            if (parameters == null)
            {
                throw new ArgumentNullException("parameters != null");
            }

            Name = name;
            XMLNS = xmlns;
            Parameters = parameters;
        }

        /// <inheritdoc />
        public bool IsValid
        {
            get
            {
                if (string.IsNullOrWhiteSpace(Name) || string.IsNullOrWhiteSpace(XMLNS))
                {
                    return false;
                }

                foreach (var parameter in Parameters)
                {
                    if (!parameter.IsValid)
                    {
                        return false;
                    }
                }

                return true;
            }
        }

        /// <inheritdoc />
        public string Name { get; }

        /// <inheritdoc />
        public IEnumerable<IMethodParameter> Parameters { get; }

        /// <inheritdoc />
        public string XMLNS { get; }
    }
}