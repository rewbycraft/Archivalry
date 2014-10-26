#API

##Direct Files
###Upload
####Request:
````
PUT /files/<filename>
````
####Result:
````
<uuid>
````
####Example:
````
$ curl -u username:password -X PUT --data-binary "@/home/user/myawesomefile.pdf" http://<host>:<port>/files/myawesomefile.pdf
bc25a993-2484-4166-9107-6b49da916821
````

###Get
####Request:
````
GET /files/<uuid>
````
Note that this is also valid:

````
GET /files/<uuid>/
````

This too (implemented because wget doesn't respect the filename returned by the server, *sigh*):

````
GET /files/<uuid>/<filename>
````

About this last one, the <filename> doesn't have to match the original filename.

####Result:
````
<file content>
````
####Example:
````
$ wget http://<host>:<port>/files/bc25a993-2484-4166-9107-6b49da916821/myawesomefile.pdf -O myawesomefile.copy.pdf
````
If you shasum ````myawesomefile.pdf```` and ````myawesomefile.copy.pdf```` you will see that their sums match.

###Delete
####Request:
````
DELETE /files/<uuid>
````
####Result:
````
File deleted.
````
####Example:
````
$ curl -u username:password -X DELETE http://<host>:<port>/files/bc25a993-2484-4166-9107-6b49da916821
File deleted.
````
