#ifndef COMMS_HANDLER_H
#define COMMS_HANDLER_H

#define  BUFFER_SIZE  1024
#define  PORT_NO 1337
#define  BAUD_RATE	B115200	
#define  SERIAL_PORT "/dev/ttyACM0"

//----- DECLARATIONS OF TCP FUNCTIONS
void tcpstart();
void tcprecv(int sock, char* buffer);
void tcpsend(int sock, char* msg);
void tcpclose(int sock);

//----- DECLARATIONS OF BT FUNCTIONS
void btstart();
void btrecv(int sock, char* buffer);
void btsend(int sock, char* msg);
void btclose(int sock);

//----- DECLARATIONS OF SRL FUNCTIONS
void srlstart();
void srlrecv(int sock, char* buffer);
void srlsend(int sock, char* msg);
void srlclose(int sock);

//----- DECLARATIONS OF TCP GLOBAL VARIABLES
char pcbuf[BUFFER_SIZE];
int tcpservfd, tcpclifd;
int pc_status;

//----- DECLARATIONS OF BT GLOBAL VARIABLES
char nxbuf[BUFFER_SIZE];
int btservfd, btclifd;
int nx_status;

//----- DECLARATIONS OF GLOBAL VARIABLES
char rbbuf[BUFFER_SIZE];
int srlfd;
int rb_status;

#endif
