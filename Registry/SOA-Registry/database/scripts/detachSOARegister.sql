USE master
GO

USE master
GO
	USE SOARegistry
		exec sp_droprolemember 'db_owner','soaRegister'
		exec sp_revokedbaccess N'soaRegister'
	GO

USE master
GO
	exec sp_droplogin N'soaRegister'
GO

USE master
GO

if exists (select * from master..sysdatabases where name = N'SOARegistry') 
  BEGIN        
      exec sp_detach_db @dbname = N'SOARegistry'
  END          
GO             

USE master
GO

