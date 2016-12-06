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

        private string _teamName;
        private string _teamID;
        private string _registryIP;
        private string _registryPort;
        private string _serviceTag;

        private string _loadingMessage;
        private IEnumerable<ResponseHolder> _resultList;
        private IWebMethod _selectedMethod;

        private IWebService _selectedService;
        private ITeam _selectedTeam;

        private IEnumerable<IWebService> _services;
        private IEnumerable<ITeam> _teams;

        public ServiceViewModel()
        {
            _teamID = "1189";
            _teamName = "BestTeam";
            _registryIP = "10.113.21.30";
            _registryPort = "3128";
            _serviceTag = "CAR-LOAN";

            _dialogService = new DialogService();
            _services = new List<IWebService>();
            _teams = new List<ITeam>();
            RefreshEnabled = true;

            _loading = false;
            _displayParameters = false;
            _loadingMessage = "Loading...";

            // RefreshConfing(null);
        }

        public IEnumerable<IWebService> Services
        {
            get { return _services; }
        }

        public IEnumerable<ITeam> Teams
        {
            get { return _teams; }
            set
            {
                _teams = value;
                
                OnPropertyChanged("Teams");
            }
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

        public string TeamName
        {
            get { return _teamName; }
            set
            {
                _teamName = value;
                
                OnPropertyChanged("TeamName");
            }
        }

        public string TeamID
        {
            get { return _teamID; }
            set
            {
                _teamID = value;

                OnPropertyChanged("TeamID");
            }
        }

        public string RegistryIP
        {
            get { return _registryIP; }
            set
            {
                _registryIP = value;

                OnPropertyChanged("RegistryIP");
            }
        }

        public string RegistryPort
        {
            get { return _registryPort; }
            set
            {
                _registryPort = value;

                OnPropertyChanged("RegistryPort");
            }
        }

        public string ServiceTag
        {
            get { return _serviceTag; }
            set
            {
                _serviceTag = value;

                OnPropertyChanged("ServiceTag");
            }
        }

        public event PropertyChangedEventHandler PropertyChanged;

        public void RefreshConfing(object obj)
        {
            LoadingMessage = "Requesting";
            IsLoading = true;

            Task.Run(DoQuery);
        }

        public void SendRequest(object obj)
        {
            LoadingMessage = "Requesting";
            IsLoading = true;

            Task.Run(DoRequest);
        }

        private Task DoQuery()
        {
            SendRequest(true);

            return null;
        }

        private Task DoRequest()
        {
            SendRequest();

            return null;
        }

        private void SendRequest(bool query = false)
        {
            string receivedMessage = null;
            ResultList = null;

            try
            {
                if (query)
                {
                    receivedMessage = HL7Helper.GenerateQueryService(TeamName, TeamID, RegistryIP, RegistryPort, ServiceTag);
                }
                else
                {
                    receivedMessage = HL7Helper.SendRequest(SelectedService);
                }
            }
            catch (Exception e)
            {
                Logger.Error("Failed to send HL7 request", e);
                _dialogService.ShowMessageBox("Failed to send HL7 request.");

                IsLoading = false;
                return;
            }

            LoadingMessage = "Analyzing";

            string error = string.Empty;

            ResponseHolder serviceResult = null;
            IWebService queryResult = null;

            if (query)
            {
                queryResult = HL7Helper.AnalyzeQueryResponse(receivedMessage, out error);
            }
            else
            {
                serviceResult = HL7Helper.AnalyzeResponse(receivedMessage, out error);
            }
            
            if (!string.IsNullOrWhiteSpace(error))
            {
                _dialogService.ShowMessageBox(string.Format("Bad respond: {0}", error));
                Logger.Error(error, new Exception(receivedMessage));
            }
            else
            {
                if (query)
                {
                    Teams = new List<ITeam>()
                    {
                        new Team(TeamName, new List<IWebService>() {queryResult})
                    }
                ;
                }
                else
                {
                    ResultList = new List<ResponseHolder>() { serviceResult };
                }
            }

            IsLoading = false;
            return;
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