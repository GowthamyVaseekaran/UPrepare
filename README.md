#UPrepare (Unsupervised Behavoiour Learning with Predictive Prevention of Resource Escalation)

This code is an automation algorithm designed to monitor the IaaS host machines for the anomalous patterns of resource usage from the VMs running on those host. This system is a proactive system which through Self Organizing Maps predicts whether the pattern of resource usage would lead to escalation in resource requirements of VM. So the algorithm would predict certain lead time and that lead time would be used to make prediction as to how much would the resource usage would be after that interval.

The prevention mechanism uses Signal Processing and Markov chaining to determine resource usage and the host is then automatically controlled to lease that many reosurces to the VM. If the resource requirements is more than it can handle then in that case VM migration can be used to solve this issue.

References:
1. [PREPARE: Predictive Performance Anomaly Prevention for Virtualized Cloud Systems]:(http://dance.csc.ncsu.edu/papers/icdcs12-prepare.pdf)
2. [UBL: Unsupervised Behavior Learning for Predicting Performance Anomalies in Virtualized Cloud Systems]: (http://dance.csc.ncsu.edu/papers/UBL.pdf)
3.[PRESS: PRedictive Elastic ReSource Scaling for cloud systems]:(http://dance.csc.ncsu.edu/papers/press.pdf)
