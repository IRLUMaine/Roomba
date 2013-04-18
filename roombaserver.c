/*
 * roombaserver.c
 * 
 * A simple TCP/IP <--> Serial proxy for ROOMBA
 * 
 * Author: Samuel A. Winchenbach
 * 
 * 
 * 
 */


#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netinet/ip.h>
#include <fcntl.h>
#include <stdio.h>
#include <asm/ioctls.h>
#include <asm/termios.h>
#include <errno.h>
#include <string.h>

const char SER_PORT_DEV[] = "/dev/usb/tts/0";
#define SERVER_PORT 1979
#define MAX_BUFF 1024

void set_rts(int fd, int level) {
	int status;
	ioctl(fd, TIOCMGET, &status);
	
	if (level != 0) {
		status |= TIOCM_RTS;
	} else {
		status &= ~TIOCM_RTS;
	}
	ioctl(fd, TIOCMSET, &status);
	return;
}


int main(int argc, char* argv[])
{
	unsigned char buffer[MAX_BUFF];
	int sockfd, insock, serfd, i, recvcnt;
	struct sockaddr_in myaddr, theiraddr;
	struct termios tc;
	socklen_t addrlen;

	
	// Init Serial Port!
	serfd = open ( SER_PORT_DEV, O_RDWR | O_NOCTTY | O_NONBLOCK);     // really ought to check for error
	tcgetattr (serfd, &tc);
	
	cfmakeraw(&tc);
	tc.c_cflag |= (CS8 | CREAD | CLOCAL);
	tc.c_cc[VTIME] = 0;
	tc.c_cc[VMIN] = 1;
	
	cfsetispeed (&tc, B115200);
	cfsetospeed (&tc, B115200);
	tcsetattr (serfd, TCSANOW, &tc);

	// Init Networking!
	sockfd = socket(PF_INET, SOCK_STREAM, 0);
	fcntl(sockfd, F_SETFL, O_NONBLOCK);
	
	myaddr.sin_family = AF_INET;
	myaddr.sin_port = htons(SERVER_PORT);
	myaddr.sin_addr.s_addr = htonl(INADDR_ANY);
	memset(myaddr.sin_zero, '\0', sizeof myaddr.sin_zero);

	bind(sockfd, (struct sockaddr*)&myaddr, sizeof myaddr);
	listen(sockfd, 0);

	while(1)
	{

		addrlen = sizeof(theiraddr);
		insock = accept(sockfd, (struct sockaddr*)&theiraddr, &addrlen);
		
		if ( (insock < 0) && ((errno == EAGAIN) || (errno == EWOULDBLOCK)) ) continue;
#ifdef DEBUG		
		printf(">>>>>>>>>> HELLO! <<<<<<<<<<\n");
#endif

        	set_rts(serfd, 0);
		usleep(100000);
		set_rts(serfd, 1);
		usleep(2000000);

		while ((recvcnt  = recv(insock, buffer, MAX_BUFF, 0)) != 0) {
		    if ( (recvcnt == -1) && (errno != EAGAIN) && (errno != EWOULDBLOCK) ) break;
		    
#ifdef DEBUG
		    for (i = 0; i < recvcnt; ++i)
		      printf("%hhu ", buffer[i]);
		    printf("\n");
#endif
		    
		    write(serfd, &buffer[0], recvcnt);
		    fflush(NULL);
		    
		    recvcnt = read(serfd, &buffer[0], MAX_BUFF);
		    if (recvcnt > 0)
		    {
		      send(insock, &buffer[0], recvcnt, 0);
		      fflush(NULL);
		    }	    
		}
#ifdef DEBUG
		printf(">>>>>>>>>> GOODBYE :( <<<<<<<<<<\n");
#endif
		close(insock);
	}

	return 0;
}
