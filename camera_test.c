#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <strings.h>

void error(char* msg) {
    perror(msg);
    exit(1);
}

int main(int argc, char* argv[]) {
    int sockfd, portno;
    struct sockaddr_in serv_addr;
    struct hostent *server;

    if (argc < 4) {
        fprintf(stderr,"Usage: %s file host port\n", argv[0]);
        exit(1);
    }

    int fd = open(argv[1], O_RDONLY);
    int n = 0;
    char buf[512];

    portno = atoi(argv[3]);
    sockfd = socket(AF_INET, SOCK_STREAM, 0);
    if (sockfd < 0)
          error("ERROR opening socket");

    server = gethostbyname(argv[2]);
    if (server == NULL) {
        fprintf(stderr,"ERROR, no such host\n");
        exit(0);
    }

    bzero((char *) &serv_addr, sizeof(serv_addr));
    serv_addr.sin_family = AF_INET;
    bcopy((char *)server->h_addr,
                  (char *)&serv_addr.sin_addr.s_addr,
                        server->h_length);
    serv_addr.sin_port = htons(portno);

    if (connect(sockfd,(struct sockaddr *)&serv_addr,sizeof(serv_addr)) < 0)
          error("ERROR connecting");


    printf("\nSent:\n\n");
    while ((n = read(fd, buf, 512)) > 0) {
        write(sockfd, buf, n);
        write(fileno(stdout), buf, n);
    }
    printf("\nReceived:\n\n");
    while ((n = read(sockfd, buf, 512)) > 0) {
        write(fileno(stdout), buf, n);
    }
    close(sockfd);
    close(fd);
    return 0;
}
