rm(list=ls(all=T))

#Carregando o pacote 
library(mirtCAT)

Dados <- read.table("Banco_500Itens.txt",header=T)
attach(Dados)

#Parametros utilizados no modelo 
a <- Parametro_a	# parâmetro de discriminação do item 
d <- Parametro_d	#Reparametrização 
g <- Parametro_c  #Parametro de acerto casual 

#Opções de resposta #nesse caso são 5 alternativas
choices <- subset(Dados[,7:11],colnames=F)

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
Candidatox <- updateDesign(CATdesign, items = c(x,x,x), responses = c(0,0,0))

#Determinando Theta inicial 
Candidatox$person$Update.thetas(Candidatox$design, Candidatox$test)

#Processo iterativo
#Pelo Critério de Máxima Informação encontra o proximo item a ser respodido por esse candidato.
findNextItem(Candidatox)

#Pergunta e resposta do candidato
Candidatox <- updateDesign(CATdesign, items = c(211), responses = c(0))

#Determinando os outros thetas 
Candidatox$person$Update.thetas(Candidatox$design, Candidatox$test)
