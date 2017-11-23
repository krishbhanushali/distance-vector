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

### 3.2 Routing Update
Routing updates are exchanged periodically between neighboring servers based on a time interval specified at the startup. In addition to exchanging distance vector updates, servers must also be able torespond to user-specified events. There are 4 possible events in this system. They can be grouped into three classes: topology changes, queries and exchange commands: (1) Topology changes refer to an updating of link status (update). (2) Queries include the ability to ask a server for its current routing table (display), and to ask a server for the number of distance vectors it has received (packets). In the case of the packets command, the value is reset to zero by a server after it satisfies the query. (3) Exchange commands can cause a server to send distance vectors to its neighbors immediately.

## 4. Server Commands/ Input format
The server supports the following commands:-
* server -t topology-file-name -i time-interval-for-step
* update server-id1 server-id2 updated-cost
* step
* packets
* display
* disable server-id
* crash

Below is the description of each of the above commands-
1. _server -t topology-file-name -i time-interval-for-step_:- 
This command starts the server after reading the topology file and gets the time interval to trigger the step process repeatedly. No other command can be executed unless this command is executed.
**_topology-file-name_** :- topology file name in which the topology is mentioned.
**_time-interval-for-step_** :- time interval to perform the step process in a repetitive manner.

2. _update server-id1 server-id2 updated-cost_:- 
This command updates the routing table of both of the servers i.e., server-id1 and server-id2 with the updated cost. Note
that this command will be issued to both server-ID1 and server-ID2 and involve them to update the cost and no other server.
For example:-
**update 1 2 3** - Assume this update command is sent from server 1 to server 2. The cost of the link is changed 3 in both of the routing tables.
Assume server 1 to have the following intial routing table before execution of any step and update command:- (The Routing table is based on the topology specified in Figure 1)

Destination ID | Next Hop ID | Cost
-------------- | ----------- | ----
1|1|0
2|2|7
3|N. A|inf
4|4|2

After the update command is sent from server 1 to server 2, the routing table of server 1 looks like below

Destination ID | Next Hop ID | Cost
-------------- | ----------- | ----
1|1|0
2|2|3
3|N. A|inf
4|4|2

And routing table of server 2 looks like below

Destination ID | Next Hop ID | Cost
-------------- | ----------- | ----
1|1|3
2|2|0
3|3|8
4|4|3

And now the network topology looks like the following figure 2.

![Figure 2. Updated network topology](/images/updated_network_topology.png)

3. _step_:- 
Send routing update to neighbors right away. Note that except this, routing updates only happen periodically.
Let me explain how the neighbors would update their routing table based on the information sent to it by its neighbors.
Assume server 2's routing table is the above updated one. Say, server 2 performs the step command. So it will send its routing table to server 1, server 3 and server 4. See below figure,

![Figure 3. Server 2 performing step](/images/step_topology_2_to_all.png)

let's assume Server 1 is having the routing table mentioned above after the update command. When server 1 receives the routing table from server 2, it then applies the [Bellman-Ford Algorithm](https://en.wikipedia.org/wiki/Bellman%E2%80%93Ford_algorithm) to update all its cost if any. It compares all of its cost to all nodes with the routing table it received. For example,
> Cost from 1 to 3 is infinity and cost from 2 to 3 is 8.
> When server 1 receives the routing table from server 2, it updates its cost to server 3 only if the cost from server 1 to server 2 plus cost from server 2 to server 3 is less than the present cost to server 3 in the routing table.
As cost from 1 to 2 is 3 and cost from 2 to 3 is 8 which is equal to (3+8) = 11. 11 is ofcourse less than infinity, thus it updates it routing table to cost to 3 as 11 and updates next hop as 2.

> NOTE : This same thing would happen when server 2 sends its routing table to server 3. Server 3 would also do the same operations.
**Thus, each server when receives routing table from its neighbors performs the Bellman-Ford Algorithm and updates its routing table if required**

4. _packets_:- Display the number of distance vector packets this server has received since the last invocation of this information.

5. _display_:- Display the current routing table. And the table should be displayed in a sorted order from small ID to big. The display should be formatted as a sequence of lines, with each line indicating: destination-server-ID next-hop-server-ID cost-of-path

6. _disable server-id_:- Disable the link to a given server. Doing this “closes” the connection to a given server with server-ID. Here need to check is if the given server is its neighbor.

7. _crash_:- “Close” all connections. This is to simulate server crashes. Close all connections on all links. The neighboring servers must handle this close correctly and set the link cost to infinity.
