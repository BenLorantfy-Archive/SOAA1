// --------------------------------------------------------------------------------------------------------------------
// <copyright file="MethodParameter.cs" company="none">
//    Copyright by Grigory Kozyrev
// </copyright>
// <summary>
//    Project: SOA_Assignment2
//    Author: Grigory Kozyrev
//    Date: 27/09/2016
//    Last Updated: 30/09/2016
// </summary>
// --------------------------------------------------------------------------------------------------------------------

#region Using

using System;
using System.Collections.Generic;
using System.Text.RegularExpressions;
using WebServiceCore.Utilities;

#endregion

namespace WebServiceCore.Models
{
    /// <summary>
    /// Standard implementation of web method parameter
    /// </summary>
    /// <seealso cref="IMethodParameter" />
    public class MethodParameter : IMethodParameter
    {
        #region Constants

        private static readonly Dictionary<string, Type> TYPES = new Dictionary<string, Type>()
        {
            {"int", typeof(int)},
            {"integer", typeof(int)},
            {"str", typeof(string)},
            {"string", typeof(string)},
            {"float", typeof(float)},
            {"double", typeof(double)},
            {"bool", typeof(bool)},
            {"boolean", typeof(bool)},
            {"long", typeof(long)},
            {"short", typeof(short)},
            {"char", typeof(char)},
        };

        private readonly Regex _ruleRegex;
        private static readonly Regex _numberRuleRegex = new Regex("^[0-9.<>=! ]+$");

        private const char SEPARATOR = ';';

        private const string LESS_THAN = "<";
        private const string GREATER_THAN = ">";
        private const string LESS_EQUAL_THAN = "<=";
        private const string GREATER_EQUAL_THAN = ">=";
        private const string EQUAL = "=";
        private const string NOT_EQUAL = "!=";

        private const string TRUE = "true";
        private const string FALSE = "false";

        private string _value;

        #endregion

        #region Constructors

        /// <summary>
        /// Initializes a new instance of the <see cref="MethodParameter"/> class.
        /// </summary>
        /// <param name="name">The parameter name.</param>
        /// <param name="type">The parameter type.</param>
        /// <param name="required">Whenever parameter is required or not.</param>
        public MethodParameter(string name, string type, bool required)
            : this(name, type, null, null, null, required)
        {
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="MethodParameter" /> class.
        /// </summary>
        /// <param name="name">The parameter name.</param>
        /// <param name="type">The parameter type.</param>
        /// <param name="value">The parameter value.</param>
        public MethodParameter(string name, string type, string value)
            : this(name, type, value, null, null, false)
        {
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="MethodParameter" /> class.
        /// </summary>
        /// <param name="name">The parameter name.</param>
        /// <param name="type">The parameter type.</param>
        /// <param name="value">The parameter value.</param>
        /// <param name="rule">The parameter validation rule.</param>
        /// <param name="help">The parameter help.</param>
        public MethodParameter(string name, string type, string value, string rule, string help, bool required)
        {
            Name = name;
            Type = type?.ToLower();
            Value = value;
            Rule = rule;
            Help = help;
            Required = required;

            if (Rule != null &&
                TYPES.ContainsKey(type.ToLower()) &&
                TYPES[type.ToLower()] == typeof(string))
            {
                try
                {
                    _ruleRegex = new Regex(rule, RegexOptions.Compiled);
                }
                catch (Exception e)
                {
                    _ruleRegex = null;
                    Logger.Debug("Failed to parse regex", e);
                }
            }
        }

        #endregion

        /// <inheritdoc />
        public string Help { get; }

        /// <inheritdoc />
        public bool IsValid
        {
            get
            {
                if (!Required
                    && string.IsNullOrWhiteSpace(Value))
                {
                    return true;
                }

                return !string.IsNullOrWhiteSpace(Name) &&
                       !string.IsNullOrWhiteSpace(Type) &&
                       ValidateType();
            }
        }

        /// <inheritdoc />
        public bool Required { get; }

        /// <inheritdoc />
        public string Name { get; }

        /// <inheritdoc />
        public string Rule { get; }

        /// <inheritdoc />
        public string Type { get; }

        /// <inheritdoc />
        public string Value
        {
            get { return _value; }
            set { _value = value ?? string.Empty; }
        }

        #region Private Members

        private bool ValidateType()
        {
            var typeString = Type.ToLower();

            if (!TYPES.ContainsKey(typeString))
            {
                return false;
            }

            var type = TYPES[typeString];

            try
            {
                if (type != typeof(string) ||
                    type != typeof(bool))
                {
                    Convert.ChangeType(Value, type);
                }
            }
            catch (Exception)
            {
                return false;
            }

            return CheckRule(type);
        }

        private bool CheckRule(Type type)
        {
            if (type == typeof(string))
            {
                return _ruleRegex == null ||
                       _ruleRegex.IsMatch(Value);
            }

            if (type == typeof(bool))
            {
                var valueLow = Value.ToLower();
                return valueLow.Equals(TRUE) || valueLow.Equals(FALSE);
            }

            if (Rule == null)
            {
                return true;
            }

            foreach (var singleRule in Rule.Split(SEPARATOR))
            {
                if (!_numberRuleRegex.IsMatch(singleRule))
                {
                    continue;
                }

                if (!CheckNumberRule(singleRule, type))
                {
                    return false;
                }
            }

            return true;
        }

        private bool CheckNumberRule(string rule, Type type)
        {
            double number = 0;
            object value = Convert.ChangeType(Value, type);

            if (rule.Contains(LESS_EQUAL_THAN))
            {
                if (!TryExtractNumber(rule, LESS_EQUAL_THAN, out number))
                {
                    return true;
                }

                return Convert.ToDouble(value) <= number;
            }
            else if (rule.Contains(GREATER_EQUAL_THAN))
            {
                if (!TryExtractNumber(rule, GREATER_EQUAL_THAN, out number))
                {
                    return true;
                }

                return Convert.ToDouble(value) >= number;
            }
            else if (rule.Contains(NOT_EQUAL))
            {
                if (!TryExtractNumber(rule, NOT_EQUAL, out number))
                {
                    return true;
                }

                return Math.Abs(Convert.ToDouble(value) - number) > 0.001;
            }
            else if (rule.Contains(LESS_THAN))
            {
                if (!TryExtractNumber(rule, LESS_THAN, out number))
                {
                    return true;
                }

                return Convert.ToDouble(value) < number;
            }
            else if (rule.Contains(GREATER_THAN))
            {
                if (!TryExtractNumber(rule, GREATER_THAN, out number))
                {
                    return true;
                }

                return Convert.ToDouble(value) > number;
            }
            else if (rule.Contains(EQUAL))
            {
                if (!TryExtractNumber(rule, EQUAL, out number))
                {
                    return true;
                }

                return Math.Abs(Convert.ToDouble(value) - number) < 0.001;
            }

            return true;
        }

        private bool TryExtractNumber(string rule, string action, out double number)
        {
            number = 0;

            int actionIndex = rule.IndexOf(action, StringComparison.Ordinal);
            if (actionIndex == -1)
            {
                return false;
            }

            var ruleValue = rule.Substring(actionIndex + action.Length).Trim();
            return double.TryParse(ruleValue, out number);
        }

        #endregion
    }
}