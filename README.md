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

License
-----------
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE SOFTWARE BE LIABLE FOR ANY DAMAGES OR OTHER LIABILITY, WHETHER IN CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE
