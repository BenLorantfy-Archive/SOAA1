USE master      
GO             
if not exists (select * from master..sysdatabases where name = N'SOARegistry') 
  BEGIN        
      exec sp_attach_db @dbname = N'SOARegistry', @filename1 = N'C:\SOA\Runtime\SOA-Registry\database\SOA-Registry.mdf', @filename2 = N'C:\SOA\Runtime\SOA-Registry\database\SOA-Registry_log.ldf' 
  END          
GO             

USE master
GO

if not exists (select * from master..syslogins where name = N'soaRegister')
BEGIN
        exec sp_addlogin N'soaRegister', N's0@R3g15t3R', N'SOARegistry', @@language
END
GO

USE master
GO
USE SOARegistry
    exec sp_grantdbaccess N'soaRegister'
    exec sp_addrolemember 'db_owner','soaRegister'
GO

USE master
GO
