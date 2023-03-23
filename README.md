JTun2Socks
===========

This is a network layer socks client, that minilpulates packets to run them through a Socks5 server.

Supports
-----------
This repo currently supports the following and I plan to implement the rest later on.

- [x] Connect
- [x] Bind
- [ ] UDP (Just forwarded past the tunnel for now)

How it works
-----------
VPNs and Sockets are on 2 different layers which makes this project a little bit difficult, however the task is not impossible. This project works by sorting packets based off of type: UDP, TCP, ICMTP. We then take all TCP packets and sort them using a NAT, this makes it easier to identify where each packet is supposed to go. Once the packets are sorted we will take the TCP packets and change the to IP address and port to a local socket being the SOCKS proxy client.
