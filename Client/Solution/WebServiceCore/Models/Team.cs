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
        }

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
