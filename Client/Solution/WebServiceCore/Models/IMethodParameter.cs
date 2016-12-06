// --------------------------------------------------------------------------------------------------------------------
// <copyright file="IMethodParameter.cs" company="none">
//    Copyright by Grigory Kozyrev
// </copyright>
// <summary>
//    Project: SOA_Assignment2
//    Author: Grigory Kozyrev
//    Date: 26/09/2016
//    Last Updated: 30/09/2016
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace WebServiceCore.Models
{
    /// <summary>
    /// Standard interface for web method parameter
    /// </summary>
    public interface IMethodParameter
    {
        /// <summary>
        /// Gets the help for parameter.
        /// </summary>
        string Help { get; }

        /// <summary>
        /// Returns true if method parameter is valid.
        /// </summary>
        /// <value>
        ///   <c>true</c> if this instance is valid; otherwise, <c>false</c>.
        /// </value>
        bool IsValid { get; }

        /// <summary>
        /// Returns true if parameter is mandatory.
        /// </summary>
        /// <value>
        ///   <c>true</c> if required; otherwise, <c>false</c>.
        /// </value>
        bool Required { get; }

        /// <summary>
        /// Gets the parameter name.
        /// </summary>
        string Name { get; }

        /// <summary>
        /// Gets the parameter validation rule.
        /// </summary>
        string Rule { get; }

        /// <summary>
        /// Gets the type of parameter.
        /// </summary>
        string Type { get; }

        /// <summary>
        /// Gets or sets the parameter value.
        /// </summary>
        string Value { get; set; }
    }
}