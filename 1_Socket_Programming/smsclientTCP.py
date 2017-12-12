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

	# In TCP, there is no such thing as an empty message, so zero means a peer disconnect.
	if (message == "") : 
		print("0 -1 INPUT_NOT_IN_RANGE")
		sys.exit()
	# message now contains the input file in a single line.
	# Running TCP client

	# Create a socket
	try:
		print("Creating TCP client socket...")
		tcp_client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
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

	# Connect to the server
	try:
		print("Connecting the server...")
		tcp_client_socket.connect(server_address)
	except socket.error, error:
		print("Connection failed: " + (str)(error))
		sys.exit()

	# Send the message
	try:
		print("Sending the message...")
		tcp_client_socket.send(message)
	except socket.error, error:
		print("Sending failed: " + (str)(error))
		sys.exit()

	# Receive the reply
	result = ""
	try:
		print("Receiving response from server")
		result = tcp_client_socket.recv(4096)
	except socket.error, error:
		print("Receive failed: " + (str)(error))
		sys.exit()

	print("Response from server:")
	print(result)
	tcp_client_socket.close()
	print("Exiting...")
	sys.exit()
