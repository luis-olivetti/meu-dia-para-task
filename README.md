# meu-dia-para-task
## Objetivo

Vai iniciar uma atividade no seu dia e depois precisa fazer o controle do horário consumido nela?
Organiza seus horários em um bloco de notas e depois precisa passar para uma planilha?

Então, este programa com nome super criativo veio para facilitar um pouco este trabalho :)

A intenção dele é bem simples, ao iniciar o programa, ele vai ficar minimizado na barra de tarefas com um ícone de relógio.
Ao clicar nele, escolha a opção "Apontar o que vou fazer" e então irá abrir esta tela:

![image](https://user-images.githubusercontent.com/5676551/199703726-cc65246e-36b8-4ee6-891e-41a151a129c3.png)

Descreva sua atividade e aperte em "Ok".

Vai almoçar/encerrar o dia?
Então marque a opção "Pausa/Encerramento".

## Configuração

Necessário instalar o Java para executar o programa. </br>
https://www.java.com/pt-BR/download/manual.jsp

Existe um arquivo "config.json" que possui as seguintes opções: </br>
"projectCode" -> Código do projeto no TASK </br>
"username" -> Usuário utilizado no TASK </br>
"teamCode" -> Código da equipe no TASK </br>

## Download

Acesse este link https://github.com/luis-olivetti/meu-dia-para-task/releases e escolha a última versão.

## Gerando .jar

Maven > Execute Maven Goal: mvn clean compile assembly:single
