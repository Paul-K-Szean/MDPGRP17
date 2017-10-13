#include "commshandler.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <fcntl.h>
#include <termios.h>

void srlstart(){
	struct termios opt;
//----- SETTING UP SERIAL PORT CONFIG
	
	//baud rate for i/o
	//cfsetospeed(&opt, BAUD_RATE);
	//cfsetispeed(&opt, BAUD_RATE);
	//parity bits
	//stop bits
	//srlfd = fopen(sr_port, "w+");
	do{
		//try to open serial port while status is 0 (disconnected)
		//retry every 3 secs
		printf("Waiting for Serial Connection...\n");
		sleep(3);
		srlfd = open(SERIAL_PORT, O_RDWR | O_NOCTTY ); 
		if (srlfd < 0){
			rb_status = 0;
			perror("ERROR opening serial port");
		}
		else{
			rb_status = 1;
			printf("Serial Connection #%d Up!\n", srlfd);
		}
	}while(!rb_status);
}

void srlrecv(int sock, char* buffer){
	 int i;
	 memset(buffer, 0, BUFFER_SIZE);
	 //read is already infinite loops
	 i = read(sock,buffer,BUFFER_SIZE);
	 printf("[%d SRL Bytes Received]: %s\n", i, buffer);
	 printf("Serial Buffer Length: %d", strlen(buffer));
	 if (i <= 0){
		perror("ERROR reading from serial port");
		srlclose(srlfd);
	 }
}

void srlsend(int sock, char* msg){
	int i;
	//char* newline = "\n";
	//char newmsg[BUFFER_SIZE];
	//strcpy(newmsg, msg); 
	//strcat(newmsg, newline);
	i = write(sock, msg, strlen(msg));
	//i = write(sock, msg, BUFFER_SIZE);
	printf("[%d SRL Bytes Sent]: %s\n", i, msg);
	if (i < 0){
		perror("ERROR writing to serial port");
		srlclose(srlfd);
	}
}

void srlclose(int fd){
	if(rb_status == 1)
		rb_status = 0;
	printf("Closing sock %d\n", fd);
	close(fd);
}
