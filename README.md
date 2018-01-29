# RE-Eval
## Relation Extraction evaluation dataset 

The following list shows the relations considered to construct this dataset. Some relations was changed for a similar relation in DBpedia.

| **Relation (r)**         | **Subject (s)**       | **Object (o)**    | **Change in DBpedia?** |  
| -----------         | -------------       | ----------    | ----------------- |
| Employer            | Person              |	Organization  | (o)dbo:Organisation |
| Occupation          | Person              | Occupation    | (o)dbo:PersonFunction |
| Nationality         | Person              | Country       | |
| Spouse              | Person              |	Person        | |
| *Children            | Person              |	Person        | (r)dbp:children*|
| Parents             | Person              |	Person        | (r)dbp:parents | 
| Cities of Residence | Person              |	City          | (r)dbo:residence |    
| Schools Attended    | Person              |	School        | (r)dbo:school |
| Awards              | Person              |	Award         | (r)dbo:award |
| *Siblings            | Person              |	Person        | (r)dbp:siblings*|    
| City of Birth       | Person              | City          | (r)dbo:birthPlace |  
| Party affiliation   | Person              | Party         | (r)dbo:party and (o) dbo:PoliticalParty |

| **Relation (dbpedia relation)** | **Number of triples found** |
| ------------------------------- | --------------------------- |
| Employer (organisation)         | 4908 |
| Occupation                      | 10000 |
| Nationality                     | 10000 |
| Spouse                          | 10000 |
| Children                        | 785 |
| Parents                         | 1546 |
| Cities of Residence (residence) | 10000 |
| Schools Attended (school)       | 3225 |
| Awards (award)                  | 10000 |
| Siblings                        | 394 |
| City of Birth (brithPlace)      | 10000 |
| Party affiliation (party)       | 10000 |
