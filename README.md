# RE-Eval
## Relation Extraction - evaluation dataset 

The following list shows the relations considered to construct this dataset. Some relations was changed for a similar relation in DBpedia.

| **Relation (r)**         | **Subject (s)**       | **Object (o)**    | **Change in DBpedia?** |  
| -----------         | -------------       | ----------    | ----------------- |
| Employer            | Person              |	Organization  | (o)dbo:Organisation |
| Occupation          | Person              | Occupation    | (o)dbo:Work |
| Nationality         | Person              | Country       | |
| Spouse              | Person              |	Person        | |
| Children            | Person              |	Person        | (r)dbo:child |
| Parents             | Person              |	Person        | (r)dbp:parents | 
| Cities of Residence | Person              |	City          | (r)dbo:residence |    
| Schools Attended    | Person              |	School        | (r)dbo:school |
| Awards              | Person              |	Award         | (r)dbo:award |
| Siblings            | Person              |	Person        | (r)dbo:child |    
| City of Birth       | Person              | City          | (r)dbo:birthPlace |  
| Party affiliation   | Person              | Party         | (r)dbo:party and (o) dbo:PoliticalParty |


The following list shows the number of triple found per relation. Those relations with an * are defined as *RDFS properties*. 

| **Original Relation**   | **DBpedia Relation**    | **Number of triples found** |
| ---------------------   | --------------------    | --------------------------- |
| Employer                | employer                | 4908 |
| Occupation              | occupation              | 1028 |
| Nationality             | nationality             | 10000 |
| Spouse                  | spouse                  | 10000 |
| Children                | child                   | 785 |
| Parents                 | * parents               | 1546 |
| Cities of Residence     | residence               | 10000 |
| Schools Attended        | school                  | 3225 |
| Awards                  | award                   | 10000 |
| Siblings                | child                   | 394 |
| City of Birth           | birthPlace              | 10000 |
| Party affiliation       | party                   | 10000 |
