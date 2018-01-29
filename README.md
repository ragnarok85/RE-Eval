# RE-Eval
## Relation Extraction evaluation dataset 

The following is the list of relations considers to construct this dataset

| **Predicate (p)**         | **Subject (s)**       | **Object (o)**    | **Change in DBpedia?** |  
| -----------         | -------------       | ----------    | ----------------- |
| Employer            | Person              |	Organization  | |
| Occupation          | Person              | Occupation    | (o)dbo:PersonFunction |
| Nationality         | Person              | Country       | |
| Spouse              | Person              |	Person        | |
| Children            | Person              |	Person        | (p)dbp:children |
| Parents             | Person              |	Person        | (p)dbp:parents | 
| Cities of Residence | Person              |	City          | (p)dbo:residence |    
| Schools Attended    | Person              |	School        | (p)dbp:schoolAttended |
| Awards              | Person              |	Award         | |
| Siblings            | Person              |	Person        | (p)dbp:siblings |    
| City of Birth       | Person              | City          | (p)dbo:birthPlace |  
| Party affiliation   | Person              | Party         | (p)dbo:party and (o) dbo:PoliticalParty |
