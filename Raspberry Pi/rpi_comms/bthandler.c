#include "commshandler.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/socket.h>
#include <bluetooth/bluetooth.h>
#include <bluetooth/hci.h>
#include <bluetooth/hci_lib.h>
#include <bluetooth/rfcomm.h>

void btstart(){
    char address[18] = { 0 };
	struct sockaddr_rc serv_addr = { 0 }, cli_addr = { 0 };
    socklen_t clilen = sizeof(cli_addr);

//----- CREATE SOCKET
    btservfd = socket(AF_BLUETOOTH, SOCK_STREAM, BTPROTO_RFCOMM);
	if (btservfd < 0)
        perror("ERROR opening socket");

//----- INIT SOCKET STRUCTURE
    serv_addr.rc_family = AF_BLUETOOTH;
    serv_addr.rc_bdaddr = *BDADDR_ANY;
    serv_addr.rc_channel = (uint8_t) 1;
	
//----- BINDING
	if (bind(btservfd, (struct sockaddr *) &serv_addr,
              sizeof(serv_addr)) < 0)
              perror("ERROR on binding");
			  
//----- LISTENING
	printf("Waiting for Bluetooth Connection...\n");
    listen(btservfd, 1);
	// accept connection
    btclifd = accept(btservfd, (struct sockaddr *)&cli_addr, &clilen);
	if (btclifd < 0){
		  nx_status = 0;
          perror("ERROR on accept");
	}
	else {
		  nx_status = 1;
		  ba2str( &cli_addr.rc_bdaddr, address );
		  printf("Bluetooth connected to: %s\n", address);
	}
    //fprintf(stderr, "accepted connection from %s\n", buf);
}

void btrecv(int sock, char* buffer){
	 int bytes_read;
	 memset(buffer, 0, BUFFER_SIZE);
	 //read is already infinite loops
	 bytes_read = read(sock,buffer,BUFFER_SIZE);
	 printf("[%d BT Bytes Received]: %s\n", bytes_read, buffer);
	 if (bytes_read < 0){
		perror("ERROR reading from socket");
		btclose(btservfd);
		btclose(btclifd);
		//printf("Shut Command Detected!\n");
	 }
}

void btsend(int sock, char* msg){
	int i;
	//char* newline = "\n";
	//char newmsg[BUFFER_SIZE];
	//strcpy(newmsg, msg); 
	//strcat(newmsg, newline);
	i = write(sock, msg, strlen(msg));
	//i = write(sock, msg, BUFFER_SIZE);
	printf("[%d BT Bytes Sent]: %s\n", i, msg);
	if (i < 0){
		perror("ERROR writing to socket");
		btclose(btservfd);
		btclose(btclifd);
	}
}

void btclose(int sock){
	if(nx_status == 1)
		nx_status = 0;
	printf("Closing sock %d\n", sock);
	close(sock);
}
