// --------------------------------------------------------------------------------------------------------------------
// <copyright file="ServiceViewModel.cs" company="none">
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
using System.Collections.Generic;
using System.ComponentModel;
using System.ComponentModel.Composition;
using System.Threading.Tasks;
using System.Windows.Input;
using SOA_Assignment2.Commands;
using WebServiceCore.Models;
using WebServiceCore.Utilities;

#endregion

namespace SOA_Assignment2.ViewModels
{
    public class ServiceViewModel : INotifyPropertyChanged
    {
        [Import] // This is MEF-specific sample
        private readonly IDialogService _dialogService;

        private bool _displayParameters;
        private bool _loading;

        private string _loadingMessage;
        private IEnumerable<ResponseHolder> _resultList;
        private IWebMethod _selectedMethod;

        private IWebService _selectedService;
        private ITeam _selectedTeam;

        private IEnumerable<IWebService> _services;
        private IEnumerable<ITeam> _teams;

        public ServiceViewModel()
        {
            _dialogService = new DialogService();
            _services = new List<IWebService>();
            _teams = new List<ITeam>()
            {
                new Team("BestTeam",
                         new List<IWebService>()
                         {
                             new WebService("192.168.238.1",
                                            2016,
                                            "PayStubCalculator",
                                            new List<IMethodParameter>()
                                            {
                                                new MethodParameter("type", "int", "1"),
                                                new MethodParameter("hours", "string", "40"),
                                                new MethodParameter("rate", "float", "14.5"),
                                            })
        })
            };
            RefreshEnabled = true;

            _loading = false;
            _displayParameters = false;
            _loadingMessage = "Loading...";

            RefreshConfing(null);
        }

        public IEnumerable<IWebService> Services
        {
            get { return _services; }
        }

        public IEnumerable<ITeam> Teams
        {
            get { return _teams; }
        }

        public ICommand RefreshCommand
        {
            get { return new RelayCommand(RefreshConfing); }
        }

        public ICommand SendCommand
        {
            get { return new RelayCommand(SendRequest); }
        }

        public IWebService SelectedService
        {
            get { return _selectedService; }
            set
            {
                _selectedService = value;

                OnPropertyChanged("SelectedService");
                OnPropertyChanged("CanSend");

                DisplayParameters = true;
            }
        }

        public ITeam SelectedTeam
        {
            get { return _selectedTeam; }
            set
            {
                _selectedTeam = value;
                
                OnPropertyChanged("SelectedTeam");
                OnPropertyChanged("CanSend");

                ResultList = null;
            }
        }

        public IWebMethod SelectedMethod
        {
            get { return _selectedMethod; }
            set
            {
                _selectedMethod = value;

                OnPropertyChanged("SelectedMethod");
                OnPropertyChanged("CanSend");

                DisplayParameters = true;
            }
        }

        public bool RefreshEnabled { get; set; }

        public IEnumerable<ResponseHolder> ResultList
        {
            get { return _resultList; }
            set
            {
                _resultList = value;

                OnPropertyChanged("ResultList");
            }
        }

        public string LoadingMessage
        {
            get { return _loadingMessage; }
            set
            {
                _loadingMessage = value;

                OnPropertyChanged("LoadingMessage");
            }
        }

        public bool IsLoading
        {
            get { return _loading; }
            set
            {
                _loading = value;

                OnPropertyChanged("IsLoading");
            }
        }

        public bool DisplayParameters
        {
            get { return _displayParameters; }
            set
            {
                _displayParameters = value;

                OnPropertyChanged("DisplayParameters");
            }
        }

        public bool CanSend
        {
            get
            {
                return SelectedService != null && SelectedService.IsValid &&
                       SelectedTeam != null && SelectedTeam.IsValid;
            }
        }

        public event PropertyChangedEventHandler PropertyChanged;

        public void RefreshConfing(object obj)
        {
            /*RefreshEnabled = false;
            OnPropertyChanged("RefreshEnabled");

            try
            {
                _services = XmlHelper.ReadConfig("config.xml");
                OnPropertyChanged("Services");
            }
            catch (Exception e)
            {
                Logger.Error("Failed to read config file", e);
                _dialogService.ShowMessageBox("Failed to read config file");
            }

            RefreshEnabled = true;
            OnPropertyChanged("RefreshEnabled");*/
        }

        public void SendRequest(object obj)
        {
            LoadingMessage = "Requesting";
            IsLoading = true;

            Task.Run(DoRequest);
        }

        private Task DoRequest()
        {
            string receivedMessage = null;
            ResultList = null;

            try
            {
                receivedMessage = HL7Helper.SendRequest(SelectedService);
            }
            catch (Exception e)
            {
                Logger.Error("Failed to send HTTP request", e);
                _dialogService.ShowMessageBox("Failed to send HTTP request.");

                IsLoading = false;
                return null;
            }

            LoadingMessage = "Analyzing";
            string example = "PUB|OK|||1|RSP|1|TotalPayValue|float|580.00|";

            string validation = string.Empty;
            if (!HttpHelper.ValidateHeader(receivedMessage, out validation))
            {
                _dialogService.ShowMessageBox(string.Format("Bad respond: {0}", validation));
                Logger.Error(validation, new Exception(receivedMessage));
            }
            else
            {
                try
                {
                    var soap = HttpHelper.ExtractSOAP(receivedMessage);

                    ResultList = XmlHelper.ConvertSoapXml(soap);
                }
                catch (Exception e)
                {
                    Logger.Error("Soap unwrap error", e);
                    _dialogService.ShowMessageBox("Couldn't analyze response.");
                }
            }

            IsLoading = false;
            return null;
        }

        public void UpdateCanSend()
        {
            OnPropertyChanged("CanSend");
        }

        // Create the OnPropertyChanged method to raise the event
        protected void OnPropertyChanged(string name)
        {
            PropertyChangedEventHandler handler = PropertyChanged;
            if (handler != null)
            {
                handler(this, new PropertyChangedEventArgs(name));
            }
        }
    }
}