rm(list=ls(all=T))

#Carregando o pacote
library(mirtCAT)

#Parametros retirados do banco de dados
connection <- odbcDriverConnect('driver={SQL Server};server=localhost;database=SCAT;trusted_connection=true')
parameters <- sqlQuery(connection , 'select Question, parametro_a, parametro_d, parametro_g, from TbMirtCat')
choices <- sqlQuery(connection , 'select Option1, Option2, Option3, Option4, Option5 from TbMirtCat')
attach(parameters)

#Parametros utilizados no modelo
a <- parametro_a    # parâmetro de discriminação do item
d <- parametro_d    #Reparametrização
g <- parametro_g  #Parametro de acerto casual

pars <- data.frame(a1=a, d=d,g=g)
mod <- generate.mirt_object(pars, '3PL')
coef(mod,simplify=T)

#Criando um data.frame com as perguntas, alternativas e a resposta correta .
df <- data.frame(Question=Question,choices,Type = 'radio',Answer=Answer,stringsAsFactors = FALSE)

#################################################################################################################
                        #Função para encontrar o próximo item a ser respondido
#Uma função que retorna o próximo item no teste adaptativo informatizado.
#Deve ser utilizado em conjunto com a função updateDesign.
#################################################################################################################

Candidatox <- mirtCAT(df, mod, criteria = 'MI',start_item = 'MI',method='EAP',design_elements = T,theta_range= c(-4,4),quadpts=20,design = list(theta_range= c(-3,3),thetas.start=0))

#Informar quais foram as primeiras perguntas respondidas e se estão certas ou erradas
Candidatox <- updateDesign(Candidatox, items = c(x,x,x), responses = c(0,0,0))

#Determinando Theta inicial
Candidatox$person$Update.thetas(Candidatox$design, Candidatox$test)

#Processo iterativo
#Pelo Critério de Máxima Informação encontra o proximo item a ser respodido por esse candidato.
findNextItem(Candidatox)

#Pergunta e resposta do candidato
Candidatox <- updateDesign(Candidatox, items = c(211), responses = c(0))

#Determinando os outros thetas
Candidatox$person$Update.thetas(Candidatox$design, Candidatox$test)
