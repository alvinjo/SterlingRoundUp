
# Round Up Service
- [Round Up Service](#round-up-service)
    * [Assumptions](#assumptions)
    * [User Workflow](#user-workflow)
    * [API Design](#api-design)
        + [Structure](#structure)
    * [Notable Edge Cases](#notable-edge-cases)
    * [Tech Decisions](#tech-decisions)
    * [Customer Feedback](#customer-feedback)
    * [TODO](#todo)




## Assumptions
1. The starling bank round up API feature does not exist.
2. Users are using this third party Round Up service to perform this round up saving feature.
3. We do not want to round up the transactions of the current (and unfinished) day.
4. In the event that a previously rounded up transaction has been refunded, do not "undo" the previous savings goal update. 
5. The round up feature is not time sensitive. Users do not need an immediate result.


## User Workflow
1. Uncle Iroh wants to round up all the transactions from the past week and add it to a specific savings goal
2. Iroh sends a request to this Round Up API providing his account and savings space id
3. The Round Up service takes this request, creates an asynch job (check if existing in DLQ) and responds with a 202 and a job id
4. The job processes asynchronously. It fetches Irohs transactions from the last week, rounds them up, calculates the difference and makes the required transaction from his account
5. Once complete, the job status is updated.
6. Uncle Iroh wants to check if the round up has completed and so makes a request to the jobs endpoint.
7. The jobs endpoint returns with a status of complete.

## API Design

| HTTP Method | Endpoint           | Example Request Body                                                                                 | Example Response Body                 |
|-------------|--------------------|------------------------------------------------------------------------------------------------------|---------------------------------------|
| GET         | /api/v1/roundupjob |                                                                                                      | {<br/>&emsp;"status":"complete"<br/>} |
| POST        | /api/v1/roundupjob | {<br/>&emsp;"start": "",<br/>&emsp;"end": ""<br/>}                                                   |                                       |
| PATCH       | /api/v1/roundupjob | {<br/>&emsp;"jobIds": [<br/>&emsp;&emsp;"2024-03-23",<br/>&emsp;&emsp;"2024-03-24"<br/>&emsp;]<br/>} |                                       |


Not using the /account endpoints since this implies that the service will be fetching the users accounts.
The problem with this is that the fetch can return multiple accounts and a decision must be made on which account to perform the round up on.
I do not believe this kind of decision should be automated since the consequences are large.
Performing the roundup on all accounts returned by the /accounts endpoint, is also not ideal since the operation would be incredibly impactful with large consequences.
The service should instead require that an account id and savings goal id is provided in order to operate on the exact resources.
These granular actions can be repeated for however many accounts the user requires.


### Structure

The code is to be split into rest service and repo layers for separation of responsibility.
The rest layer is for web controllers and are the entry point to the system. They define the API endpoints that users will operate.
The service layer is for business logic and for processing data from the repo layer.
The repo layer is for fetching data, typically from databases or via network calls.

#### Domain Driven Design
The API should be structured by features.
This means having a parent feature package com.alvin.roundup and then within this feature package, we have our rest, service and repo packages.
If this service extends to support more features, this should be done within a separate package (e.g. com.alvin.newfeature).
This is useful in the event that the monolith service requires splitting into a separate microservice as it becomes easier to rip out a single package instead of individual classes.





RoundUpJob table design

|    | Column |        |
|----|--------|--------|
| PK | Date   |        |
| SK | Status |        |


## Notable Edge Cases
- We receive 0 transactions.
- We receive a transaction where money has come into the account instead of out.
- Thousands of transactions received from the week (careful memory!).
  - process each day asynch using sqs messages.
  - return response to user with job ids and continue processing asynch.
  - add endpoint for checking job status (webhook?)
  - ...beginning to consider the alternative approach of processing the week as a whole and having auto-scaling policies trigger for large jobs. 
- Fail to round up a transaction.
  - Consider retries, message DLQ.
- We call round up on the same transaction.
  - Each request must be idempotent. This requires persistent storage for keeping track.
- We receive transactions that have not been settled.
  - We create the job but send to DLQ. An incorrect round up is worse than a failed round up (from my perspective as a user)
- What happens when the total round up amount is more than the accounts current balance?
  - Put message in DLQ or delete, we do not want a scenario where the user is low on funds and cannot use their account because of queued messages being processed. 
  - Each job is a days worth of transactions, so it is important not to interfere with this potential living fund. 
  - DLQ re-driving is an issue since we may process stale transaction data. (What is stale data? Just unsettled transactions?)
    - We shouldn't have the message consumer do a fresh fetch each time since it does not scale well, and we could hit our api rate limit. Because of this, transaction data should stay in the message.


## Tech Decisions


## Customer Feedback
>**Uncle Iroh**
> 
>I don't like manually performing a savings round up of my week in order to achieve my savings goal target.
It is difficult to maintain a weekly budget when the round up is performed at the end of the week instead of immediately after a transaction.
I have a £3 weekly budget. I spend £1.50 on tea, and then I spend another £1.50 on tea which is within my budget. The round up then makes both of these
transactions worth £2, making my tea expenses £4 which is over budget!.



## TODO

- finish docs
  - https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet
  - https://ecotrust-canada.github.io/markdown-toc/
  - Type &nbsp;  1 space. 
  - Type &ensp;  2 spaces. 
  - Type &emsp;  3 spaces.
- webhook for checking job status?
- add log lines, follow from endpoint to repo, purposeful and efficient logs
- endpoint to fetch the days that have been processed