# Distance Vector Protocol Algorithm implementation

## 1. Problem Statement
In this assignment you will implement a simplified version of the Distance Vector Routing Protocol.
The protocol will be run on top of four servers/laptops (behaving as routers) using TCP or UDP. Each
server runs on a machine at a pre-defined port number. The servers should be able to output their
forwarding tables along with the cost and should be robust to link changes. A server should send out
routing packets only in the following two conditions: **a) periodic update** and **b) the user uses
command asking for one**. This is a little different from the original algorithm which immediately sends
out update routing information when routing table changes.

## 2. Getting Started
[A distance vector routing protocol](https://en.wikipedia.org/wiki/Distance-vector_routing_protocol) uses the [Bellman-Ford Algorithm](https://en.wikipedia.org/wiki/Bellman%E2%80%93Ford_algorithm) or [Ford-Fulkerson Algorithm](https://en.wikipedia.org/wiki/Ford%E2%80%93Fulkerson_algorithm) or [Diffusing update Algorithm](https://en.wikipedia.org/wiki/Diffusing_update_algorithm)
