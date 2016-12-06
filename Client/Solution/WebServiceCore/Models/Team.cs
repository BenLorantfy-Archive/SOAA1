using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace WebServiceCore.Models
{
    public class Team : ITeam
    {
        public Team(string name, IEnumerable<IWebService> services)
        {
            Name = name;
            Services = services;
            ID = 0;
            SecurityLevel = 1;
        }

        /// <inheritdoc />
        public int ID { get; }

        /// <inheritdoc />
        public int SecurityLevel { get; }

        /// <inheritdoc />
        public string Name { get; }

        /// <inheritdoc />
        public IEnumerable<IWebService> Services { get; }

        public bool IsValid
        {
            get
            {
                if (string.IsNullOrWhiteSpace(Name))
                {
                    return false;
                }

                return true;
            }
        }
    }
}
