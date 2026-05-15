<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Login</title>
<!-- Spring Boot/Thymeleaf often resolve this automatically with webjars-locator -->
<link rel="stylesheet" 
      href="webjars/bootstrap/5.3.8/css/bootstrap.css" />

</head>
<body>
    <div class="container mt-5">
        <div class="row justify-content-center">
            <div class="col-md-6 col-lg-4">
                <div class="card shadow">
                    <div class="card-body">
                        <h2 class="card-title text-center mb-4">Login</h2>
                        
                        <form method="post" action="j_security_check">
                            <div class="mb-3">
                                <label for="username" class="form-label">Usuário:</label>
                                <input type="text" 
                                       class="form-control" 
                                       id="username" 
                                       name="j_username" 
                                       required>
                            </div>
                            
                            <div class="mb-4">
                                <label for="password" class="form-label">Senha:</label>
                                <input type="password" 
                                       class="form-control" 
                                       id="password" 
                                       name="j_password" 
                                       required>
                            </div>
                            
                            <div class="d-grid gap-2">
                                <button type="submit" class="btn btn-primary btn-lg">Entrar</button>
                            </div>
                        </form>
                        
                        <div class="mt-4 text-center">
                            <p class="mb-0">
                                <a href="/app/HelloWorld" class="btn btn-outline-secondary btn-sm">
                                    Hello World!
                                </a>
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
