rm(list=ls(all=T))

#Carregando o pacote
if(!require(RODBC)){install.packages("RODBC"); library(RODBC)}
if(!require(mirtCAT)){install.packages("mirtCAT"); library(mirtCAT)}

#Parametros retirados do banco de dados
connection <- odbcConnect(dsn="SCAT")
parameters <- sqlQuery(connection , 'select Question, parametro_a, parametro_d, parametro_g from TbMirtCat')
choices <- sqlQuery(connection , 'select Option1, Option2, Option3, Option4, Option5 from TbMirtCat')
attach(parameters)

#Parametros utilizados no modelo
a <- parametro_a    #Parâmetro de discriminação do item
d <- parametro_d    #Reparametrização
g <- parametro_g    #Parametro de acerto casual (chute)

pars <- data.frame(a1=a, d=d,g=g)
mod <- generate.mirt_object(pars, '3PL')
coef(mod,simplify=T)

#Criando um data.frame com as perguntas, alternativas e a resposta correta .
df <- data.frame(Question=Question,choices,Type = 'radio',stringsAsFactors = FALSE)

#################################################################################################################
                        #Função para encontrar o próximo item a ser respondido
#Uma função que retorna o próximo item no teste adaptativo informatizado.
#Deve ser utilizado em conjunto com a função updateDesign.
#################################################################################################################

Candidato <- mirtCAT(df, mod, criteria = 'MI',start_item = 'MI',method='EAP',design_elements = T,theta_range= c(-4,4),quadpts=20,design = list(theta_range= c(-3,3),thetas.start=0))

#Informar quais foram as primeiras perguntas respondidas e se estão certas ou erradas
Candidato <- updateDesign(Candidato, items = c(1,2,3), responses = c(1,0,0))

#Determinando Theta 
Candidato$person$Update.thetas(Candidato$design,Candidato$test)

Teta_Estimado <- Candidato$person$thetas

#Processo iterativo
#Pelo Critério de Máxima Informação encontra o proximo item a ser respodido por esse candidato.
findNextItem(Candidato)