# README #

This README would normally document whatever steps are necessary to get your application up and running.

### What is this repository for? ###

* This repository is a controller for engine backtest, it accomplishes the following tasks:
	* Read and send market data and transaction data to MatchMaker
	* Send order to engine
	* Sync time with Engine and Calc 

* Control of Time. If timestamp of data is not processed, it would be very hard to backtest anything. For example, processing noon time, will require time sync for this controller, calc, engine and mm, may need to jump time, which would further cause exception in fix sessions. So time is adjusted for market data, transaction data and orders.
	* Any data during 1130 to 1300 is removed. (Marked as invalid)
	* Any data/order after 1300 is shifted forward by 90 minutes.
	* Any data/order during Am Auction is shifted afterward by 4 minutes.

### How do I get set up? ###

* Summary of set up
* Configuration
* Dependencies
* Database configuration
* How to run tests
* Deployment instructions

### Contribution guidelines ###

* Writing tests
* Code review
* Other guidelines

### Who do I talk to? ###

* Repo owner or admin
* Other community or team contact