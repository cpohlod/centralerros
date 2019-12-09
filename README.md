# Backend Central de Erros


Login:

	url: https://centralerros.herokuapp.com/login
	método: POST
	body: json	{"name":"zeca", "email":"zeca@gmail.com","pwd":"senha123"}
	retorno: token

Cadastro de usuário 	
	url: https://centralerros.herokuapp.com/savelogin/<token>
	body: json	{"name":"novo_usuario", "email":"novo@gmail.com","pwd":"senha124"}
	método: POST
   
Consulta todos usuário:
	url: https://centralerros.herokuapp.com/logins/<token>
	método: GET
	retorno: json com todos os usuários
	[
    {
        "id": 1,
        "name": "cleverson",
        "email": "cleverson@softplan.com.br",
        "pwd": "123"
    }
	]
	
	exemplo:
	https://centralerros.herokuapp.com/logins/8c595375991ad5b5f163fa04f52a34f63a00599ee9600c77efd53ec1cd0b3308
	
Consulta de logs:

	url: https://centralerros.herokuapp.com/logs/<token>
	método: GET
	retorno: json com os dados do log
	[
    {
        "id": 2,
        "name": "com.zaxxer.hikari.HikariDataSource - HikariPool-1 - Starting...",
        "type": "INFO",
        "orign": null,
        "story": null,
        "quantity": 1,
        "createDate": null
    },
    ]
    
	exemplo:
	https://centralerros.herokuapp.com/logs/8c595375991ad5b5f163fa04f52a34f63a00599ee9600c77efd53ec1cd0b3308
