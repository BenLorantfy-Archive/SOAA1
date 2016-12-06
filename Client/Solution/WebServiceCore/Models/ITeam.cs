using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace WebServiceCore.Models
{
    public interface ITeam
    {
        int ID { get; }
        
        int SecurityLevel { get; }

        string Name { get; }

        IEnumerable<IWebService> Services { get; }

        bool IsValid { get; }
    }
}
