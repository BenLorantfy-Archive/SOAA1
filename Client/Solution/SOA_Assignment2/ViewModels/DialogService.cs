// --------------------------------------------------------------------------------------------------------------------
// <copyright file="DialogService.cs" company="none">
//    Copyright by Grigory Kozyrev
// </copyright>
// <summary>
//    Project: SOA_Assignment2
//    Author: Grigory Kozyrev
//    Date: 29/09/2016
//    Last Updated: 30/09/2016
// </summary>
// --------------------------------------------------------------------------------------------------------------------

#region Using

using System.Windows;

#endregion

namespace SOA_Assignment2.ViewModels
{
    public class DialogService : IDialogService
    {
        /// <inheritdoc />
        public void ShowMessageBox(string message)
        {
            MessageBox.Show(message);
        }
    }
}