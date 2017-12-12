import socket
import sys

if __name__ == "__main__":

	args = sys.argv[1:]
	# Invalid input
	if len(args) < 3:
		print("Invalid input")
		print("Please use: HOST_NAME + PORT + FILE_NAME")
		sys.exit()

	# unpacking input
	server_name = args[0]
	port = (int)(args[1])
	file = args[2]

	# Invalid port
	if (port < 1024 or port > 65535):
		print("Invalid port number, each registered port is in range 1024-65535")
		sys.exit()

	# Process input file 
	message = ""
	try:
		temp = open(file, 'r')
		for line in temp:
			line = line.replace('\n',"")
			message = message + (str)(line) + " "
		temp.close()
	except IOError, error:
		print("File I/O error: " + (str)(error))
		sys.exit()

	# message now contains the input file in a single line.
	# Running UDP client

	# Create a socket
	try:
		print("Creating UDP client socket...")
		udp_client_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
	except socket.error, error:
		print("Can not create an UDP client socket: " + (str)(error))
		sys.exit()

	# Get server's address
	try:
		print("Fetching server's ip...")
		server_ip = socket.gethostbyname(server_name)

	except socket.gaierror, error:
		print("Can not get server's ip: " + (str)(error))
		sys.exit()

	# Pack ip and port
	server_address = (server_ip, port)
	# Set a 5 seconds limit
	udp_client_socket.settimeout(5)
	# Send message to server
	try:
		udp_client_socket.sendto(message, server_address)
	except socket.error, error:
		print("Sending failed: " + (str)(error) + ", exiting...")
		sys.exit()


	# Receive results. Try 3 times
	tries = 0
	result = ""
	while tries < 4:
		try:
			print("Receiving message...")
			# 1000 char at most, each 4 bits => 4096 bits is sufficient
			result, that_server_address = udp_client_socket.recvfrom(4096)
			if (result != ""): break
		except socket.timeout:			
			tries = tries + 1
			print("Timed out, tries: " + (str)(tries))
			if (tries == 3) :
				print("Reached limited trail times. exiting...")
				udp_client_socket.close()
				sys.exit()
			continue


	print("Response from server:")
	print(result)
	udp_client_socket.close()
	print("Exiting...")
	sys.exit()






