Georgia Institute of Technology
CS 3251 Programming	Assignment	1:	Basics	of	Socket	Programming
2017-2-14
Name: An Jihai
GTID: 903056575
GT Username: jan61
GT email: jan61@gatech.edu


Files:
	smsengineUDP.py		:	the python file for a server using UDP.
	smsclientUDP.py 	:	the python file for a client using UDP.

	smsengineTCP.py 	: 	the python file for a server using TCP.
	smsclientTCP.py 	: 	the python file for a client using TCP.

	README.txt			: 	this file
	sample.txt 			:	the sample output including all 4 python files.


Instructions:
	All server-side commands: 
		python smsengineUDP.py <PORT_NUMBER> <FILE_NAME>
		python smsengineTCP.py <PORT_NUMBER> <FILE_NAME>

		Example: $ python smsengineUDP.py 1028 spam.txt
				 $ python smsengineTCP.py 1028 spam.txt

		Note: the PORT_NUMBER should be any integer between 1024-65535.
			  the FILE_NAME should be an '.txt' file.
			  any invalid argument(input) will make the program stop and output an error message.

	All client-side commands: 
		python smsclientUDP.py <SERVER_NAME> <PORT_NUMBER> <FILE_NAME>
		python smsclientTCP.py <SERVER_NAME> <FILE_NAME>

		Example: $ python smsclientUDP.py networklab1.cc.gatech.edu 1028 msg.txt 
				 $ python smsclientTCP.py networklab1.cc.gatech.edu 1028 msg.txt

		Note: the SERVER_NAME should be a valid server name.
			  the PORT_NUMBER should be any integer between 1024-65535.
			  the FILE_NAME should be an '.txt' file.
			  any invalid argument(input) will make the program stop and output an error message.

Known limitations:
	
	for every 'recv()' or 'recvfrom()' method, I used 4096, even 8192 as the maximum size. Generally these values are sufficient; However there may be bugs when inputing tremendous files.

	Also note, for the UDP, I have set a time limit of 5 seconds.
