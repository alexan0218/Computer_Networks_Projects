import socket
import sys
import string

if __name__ == "__main__":

	args = sys.argv[1:]
	# Invalid input
	if (len(args) < 2):
		print("Invalid input:")
		print("Please use: PORT + FILE_NAME")
		sys.exit()
	# Unpacking inputs
	port = (int)(args[0])
	file = args[1]

	# Invalid port
	if (port < 1024 or port > 65535):
		print("Invalid port number, each registered port is in range 1024-65535")
		sys.exit()
	# Invalid file
	if (file[-4:] != ".txt"):
		print("Unrecognizable file type, please use '.txt' files")
		sys.exit()

	# Create bag of spam words
	spam = []
	try:
		temp = open(file, 'r')
		for line in temp:
			line = line.replace('\n',"")
			spam.append((str)(line.lower()))
		temp.close()
	except IOError, error:
		print("File I/O error: " + (str)(error))
		sys.exit()

	# Now spam is a list of given spam words
	# Running UDP server 

	# Create a socket
	try:
		print("Creating UDP server socket...")
		udp_server_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
	except socket.error, error:
		print("Can not create the server socket for now: " + (str)(error))
		sys.exit()

	# Get the ip address
	server_ip = socket.gethostname()
	#server_ip = "127.0.0.1"
	server_address = (server_ip, port)
	# Bind the socket with server address
	try:
		print("Binding the socket and the address")
		udp_server_socket.bind(server_address)
		print(server_address)
	except socket.error, error:
		print("Binding failed: " + (str)(error))
		sys.exit()
	
	# Run the UDP server socket
	while 1:
		is_valid_string = True

		try:
			print("receiving data...")
			# 1000 char at most, each 4 bits => 4096 bits is sufficient
			data, client_address = udp_server_socket.recvfrom(8192)
		except socket.error, error:
			print("Receiving failed: " + (str)(error))
			continue

		# Prepare to respond
		# Oversize string
		print(len(data))
		if (len(data)) <= 0 or len(data) > 1000:
			try:
				udp_server_socket.sendto("0 -1 INPUT_NOT_IN_RANGE", client_address)
				is_valid_string = False
				print("special message replied")
				print("Error: INPUT_NOT_IN_RANGE")
				continue
				
			except socket.error, error:
				print("Error occurs when sending special message: " + (str)(error))
				sys.exit()
		# Valid string
		response = ""

		data = data.translate(None, string.punctuation)
		message = data.split(" ")
		total_num = len(message) - 1
		total_spam = len(spam)
		spam_score = 0.0
		spam_word_found = ""
		
		for word in message:
			# ascii
			if (all(ord(char) < 128 for char in word)):
				if word.lower() in spam:
					spam_score += 1
					spam_word_found += word + " "
			else:
				try:
					udp_server_socket.sendto("0 -1 NON_ASCII_CHAR", client_address)
					is_valid_string = False
					print("special message replied")
					response = "Error: NON_ASCII_CHAR"
					continue
				except socket.error, error:
					print("Error occurs when sending special message: " + (str)(error))
					sys.exit()

		if is_valid_string :
			spam_score = spam_score/total_num
			response = (str)(spam_score) + " " + (str)(total_spam) + " " + spam_word_found
			try:
				print("Analyzed successfully, sending back response...")
				udp_server_socket.sendto(response, client_address)
			except socket.error, error:
				print("Failed sending: " + (str)(error))
				continue

		print("Sending back successfully:")
		print(response)
		

















