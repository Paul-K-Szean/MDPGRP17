#include "commshandler.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>

void tcpstart(){
//----- DECLARATIONS OF VARIABLES
     int portno;
     socklen_t clilen;
	 struct sockaddr_in serv_addr, cli_addr;
     //char buffer[BUFFER_SIZE];
	 /*
	 struct sockaddr_in {
        short   sin_family;
        u_short sin_port;
        struct  in_addr sin_addr;
        char    sin_zero[8];
	 };
	 */
//------------------------------
	 tcpclose(tcpservfd);
	 tcpclose(tcpclifd);
//----- CREATE SOCKET
//		UNIX for unix domain/INET for internet domain
//		STREAM for TCP/DGRAM for UDP
     tcpservfd = socket(AF_INET, SOCK_STREAM, 0);
     if (tcpservfd < 0)
        perror("ERROR opening socket");
//------------------------------

//----- INIT SOCKET STRUCTURE

	 //bzero deprecated function
     memset((char *) &serv_addr, 0, sizeof(serv_addr));
	 //atoi convert (array)string (to) (i)integer
     //portno = atoi(argv[1]);
	 portno = PORT_NO;
     serv_addr.sin_family = AF_INET;
     serv_addr.sin_addr.s_addr = INADDR_ANY;
	 //htons necessary: convert to network byte order
     serv_addr.sin_port = htons(portno);
//------------------------------
	 
//----- BINDING
     if (bind(tcpservfd, (struct sockaddr *) &serv_addr,
              sizeof(serv_addr)) < 0)
              perror("ERROR on binding");
//------------------------------
	 
//----- LISTENING
	 printf("Waiting for TCP Connection...\n");
	 //listen will block the process until it connects
	 listen(tcpservfd,1);
     clilen = sizeof(cli_addr);
     tcpclifd = accept(tcpservfd,
                 (struct sockaddr *) &cli_addr,
                 &clilen);
     if (tcpclifd < 0){
		  pc_status = 0;
          perror("ERROR on accept");
	 }
	 else{
		  pc_status = 1;
		  printf("TCP Connection #%d Up!\n", tcpclifd);
	 }
//------------------------------
}

void tcprecv(int sock, char* buffer){
	 int bytes_read;
	 memset(buffer, 0, BUFFER_SIZE);
	 //read blocks the process until something comes in
	 bytes_read = read(sock,buffer,BUFFER_SIZE);
	 printf("[%d TCP Bytes Received]: %s\n", bytes_read, buffer);
	 if (bytes_read < 0){
		perror("ERROR reading from socket");
		tcpclose(tcpservfd);
		tcpclose(tcpclifd);
	 }
}

void tcpsend(int sock, char* msg){
	int i;
	//new line is necessary for client-side code to read
	char* newline = "\n";
	char newmsg[BUFFER_SIZE];
	strcpy(newmsg, msg); 
	strcat(newmsg, newline);
	i = write(sock, newmsg, strlen(newmsg));
	printf("[%d TCP Bytes Sent]: %s\n", i, msg);
	if (i < 0){
		perror("ERROR writing to socket");
		tcpclose(tcpservfd);
		tcpclose(tcpclifd);
	}
}

void tcpclose(int sock){
	if(pc_status == 1)
		pc_status = 0;
	printf("Closing sock %d\n", sock);
	close(sock);
}
