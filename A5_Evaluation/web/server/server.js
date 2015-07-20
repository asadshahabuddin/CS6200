/*
    Author : Asad Shahabuddin
    Created: Jul 18, 2015
*/

var express    = require("express");
var bodyParser = require("body-parser");
var fs         = require("fs");
var app        = express();

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended: true}));

/* Output directory */
DIR_QRELS = "E:/Home/Repository/Java/IdeaProjects/A5_Evaluation/";

/* Author */
var author = {
    fname: "Asad",
    lname: "Shahabuddin"
};

/* ===================== */
/* API LISTENERS : BEGIN */
/* ===================== */

/* POST listeners : BEGIN */
app.post("/api/write", function(req, res)
{
	fs.writeFile(DIR_QRELS + "qrels.txt", req.body, function(err)
	{
		console.log("[error]" + err);
	});
	console.log("[echo] File system write successful");
});
/* POST listeners : END */

/* GET listeners : BEGIN */
app.get("/api", function(req, res)
{
	res.send("<body style='font-family: Arial;'><h1>Farpoint</h1><p style='font-size: 18px;'>Search API</p></body>");
});

app.get("/api/author", function(req, res)
{
    res.json(author);
});
/* GET listeners : END */

/* ===================== */
/* API LISTENERS : END */
/* ===================== */

/* Main */
app.listen(3000);
/* End of server.js */