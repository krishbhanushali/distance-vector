# Distance Vector Protocol Algorithm Implementation

## 1. Problem Statement
In this assignment you will implement a simplified version of the Distance Vector Routing Protocol.
The protocol will be run on top of four servers/laptops (behaving as routers) using TCP or UDP. Each
server runs on a machine at a pre-defined port number. The servers should be able to output their
forwarding tables along with the cost and should be robust to link changes. A server should send out
routing packets only in the following two conditions: **a) periodic update** and **b) the user uses
command asking for one**. This is a little different from the original algorithm which immediately sends
out update routing information when routing table changes.

## 2. Getting Started
[A distance vector routing protocol](https://en.wikipedia.org/wiki/Distance-vector_routing_protocol) uses the [Bellman-Ford Algorithm](https://en.wikipedia.org/wiki/Bellman%E2%80%93Ford_algorithm) or [Ford-Fulkerson Algorithm](https://en.wikipedia.org/wiki/Ford%E2%80%93Fulkerson_algorithm) or [Diffusing update Algorithm](https://en.wikipedia.org/wiki/Diffusing_update_algorithm). _We will be using Bellman-Ford Algorithm to calculate the cost of the paths._
A distance vector routing protocol only works on the basis of sending the routing table to its neighbors periodically or if there are any updates in the table. Initially, each server/node is going to have no information about the topology except its neighbors. Each server gets information about its whole topology, when each server starts to send information about its neighbors.
_Examples of distance vector routing protocols are [RIPv1, RIPv2](https://en.wikipedia.org/wiki/Routing_Information_Protocol), [IGRP](https://en.wikipedia.org/wiki/Interior_Gateway_Routing_Protocol) and [EIGRP](https://en.wikipedia.org/wiki/Enhanced_Interior_Gateway_Routing_Protocol)_.

## 3. Protocol Specification
### 3.1 Topology Establishment
I used 4 servers/computers/laptops to implement the simulation. **The four servers are required to form a network topology as shown in Figure 1**. Each server is supplied with a topology file at startup that it uses to build its initial routing table. The topology file is local and contains the link cost to the neighbors. For all other servers in the network, the initial cost would be infinity. Each server can only read the topology file for itself. The entries of a topology file are listed below:
* num-servers
* num-neighbors
* server-ID server-IP server-port
* server-ID1 server-ID2 cost
**num-servers:** total number of servers in the network.
**num-neighbors:** the number of directly linked neighbors of the server.
**server-ID, server-ID1, server-ID2:** a unique identifier for a server, which is assigned by you.
**cost:** cost of a given link between a pair of servers. Assume that cost is an integer value.
Here is an example, consider the topology in Figure 1. We give a topology file for server 1 as shown
in the table below.
![Figure 1. The network topology](/images/network_topology1.png)

Line Number| Line Entry | Comments
---------- | ---------- | --------
1|4|number of servers
2|3|number of edges or neighbors
3|1 192.168.0.112 2000|server-id1 and corresponding IP and port
4|2 192.168.0.100 2001|server-id2 and corresponding IP and port
5|3 192.168.0.118 2002|server-id3 and corresponding IP and port
6|4 192.168.0.117 2003|server-id4 and corresponding IP and port
7|1 2 7| server-id and neighbor-id and cost
8|1 4 2| server-id and neighbor-id and cost

### IMPORTANT: 
In this environment, costs are bi-directional i.e. the cost of a link from A-B is the same for B-A. Whenever a new server is added to the network, it will read its topology file to determine who its neighbors are. Routing updates are exchanged periodically between neighboring servers. When this newly added server sends routing messages to its neighbors, they will add an entry in their routing tables corresponding to it. Servers can also be removed from a network. When a server has been removed from a network, it will no longer send distance vector updates to its neighbors. When a server no longer receives distance vector updates from its neighbor for three consecutive update intervals, it assumes that the neighbor no longer exists in the network and makes the appropriate changes to its routing table (link cost to this neighbor will now be set to infinity but not remove it from the table). This information is propagated to other servers in the network with the exchange of routing updates. Please note that although a server might be specified as a neighbor with a valid link cost in the topology file, the absence of three consecutive routing updates from this server will imply that it is no longer present in the network.
