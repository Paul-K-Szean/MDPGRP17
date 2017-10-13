#include "commshandler.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <pthread.h>

//----- DECLARATIONS OF FUNCTIONS
void *recv_from_pc();
void *recv_from_nx();
void *recv_from_rb();
void *send_to_pc();
void *send_to_nx();
void *send_to_rb();
void *listen_pc();
void *listen_nx();
void *listen_rb();
void routeData();

//----- DECLARATIONS OF GLOBAL VARIABLES

void err(const char *msg){
    perror(msg);
    exit(1);
}

//----- MULTITHREADING TEMPLATE
// pthread_t tid;
// pthread_create(&tid, NULL, thread_name, NULL);
// pthread_join(tid, NULL);
// void *thread_name(){}

//1st argument = address of thread
//2nd argument = NULL
//3rd argument = address of function
//4th argument = address of parameter to pass to function

int main(){
	 pthread_t stopc, stonx, storb;
	 pthread_t lpc, lnx, lrb;
	 //pc
	 pthread_create(&lpc, NULL, listen_pc, NULL);
	 //nexus
	 pthread_create(&lnx, NULL, listen_nx, NULL);
	 //arduino
	 pthread_create(&lrb, NULL, listen_rb, NULL);

	 pthread_join(lpc, NULL);
	 pthread_join(lnx, NULL);
	 pthread_join(lrb, NULL);

	 //=================================================
	 //pthread_create(&stopc, NULL, recv_from_pc, NULL);
	 //pthread_create(&stonx, NULL, recv_from_pc, NULL);
	 //pthread_create(&storb, NULL, recv_from_pc, NULL);

	 //pthread_join(stopc, NULL);
	 //pthread_join(stonx, NULL);
	 //pthread_join(storb, NULL);
     return 0;
}

void *listen_pc(){
	//freopen("debug.log", "w+", stdout);
	while(1){
		tcpstart();
		pthread_t rfpc;
		pthread_create(&rfpc, NULL, recv_from_pc, NULL);
		pthread_join(rfpc, NULL);
	}
}

void *listen_nx(){
	//freopen("debug.log", "w+", stdout);
	while(1){
		btstart();
		pthread_t rfnx;
		pthread_create(&rfnx, NULL, recv_from_nx, NULL);
		pthread_join(rfnx, NULL);
	}
}

void *listen_rb(){
	while(1){
		srlstart();
		pthread_t rfrb;
		pthread_create(&rfrb, NULL, recv_from_rb, NULL);
		pthread_join(rfrb, NULL);
	}
}

void *recv_from_pc(){
	printf("Waiting for PC transmission...\n");
	while (pc_status){
		 tcprecv(tcpclifd, pcbuf);
		 //if buffer has something then send
		 //tcpsend(tcpclifd, pcbuf);
		 //send nx
		 routeData(pcbuf);
	}
}

void *recv_from_nx(){
	printf("Waiting for Nexus transmission...\n");
	while (nx_status){
		btrecv(btclifd, nxbuf);
		//if buffer has something then send
		//btsend(btclifd, nxbuf);
		//send pc
		if(strcmp(nxbuf, "killme") == 0){
			nx_status = 0;
			system("shutdown -h now");
		}
		routeData(nxbuf);
	}
}

void *recv_from_rb(){
	printf("Waiting for Robot transmission...\n");
	while (rb_status){
		srlrecv(srlfd, rbbuf);
		//if buffer has something then send
		//srlsend(srlfd, rbbuf);
		//sleep(1);
		routeData(rbbuf);
	}
}

void routeData(char* buffer){
	switch(buffer[0]){
		case 'a':
		case 'A':
			printf("Relaying message to Nexus\n");
			//send to Nexus
			if (btclifd > 0)
				btsend(btclifd, buffer+1);
			else
				printf("No Bluetooth Connection Detected!\n");
			break;

		case 'b':
		case 'B':
			printf("Relaying message to Arduino\n");
			//send to Arduino
			if (srlfd > 0)
				srlsend(srlfd, buffer+1);
			else
				printf("No Serial Port Connection Detected!\n");
			break;

		case 'c':
		case 'C':
			printf("Relaying message to PC\n");
			//send to PC
			if (tcpclifd > 0)
				tcpsend(tcpclifd, buffer+1);
			else
				printf("No TCP Client Connection Detected!\n");
			break;

		default:
			printf("Invalid [%c] Source Identifier!\n", buffer[0]);
	}
}

void *send_to_pc(){

}

void *send_to_nx(){

}

void *send_to_rb(){

}
