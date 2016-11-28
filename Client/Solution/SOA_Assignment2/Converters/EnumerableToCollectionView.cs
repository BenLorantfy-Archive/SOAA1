// --------------------------------------------------------------------------------------------------------------------
// <copyright file="EnumerableToCollectionView.cs" company="none">
//    Copyright by Grigory Kozyrev
// </copyright>
// <summary>
//    Project: SOA_Assignment2
//    Author: Grigory Kozyrev
//    Date: 28/09/2016
//    Last Updated: 30/09/2016
// </summary>
// --------------------------------------------------------------------------------------------------------------------

#region Using

using System;
using System.Collections;
using System.Globalization;
using System.Windows.Data;

#endregion

namespace SOA_Assignment2.Converters
{
    public class EnumerableToCollectionView : IValueConverter
    {
        /// <inheritdoc />
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            if (!(value is IEnumerable))
            {
                return null;
            }

            return new CollectionView((IEnumerable) value);
        }

        /// <inheritdoc />
        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }
    }
}