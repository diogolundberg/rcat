rm(list=ls(all=T))

#Carregando o pacote 
library(mirtCAT)

Dados <- read.table("Banco_500Itens.txt",header=T)
attach(Dados)

#Parametros utilizados no modelo 
a <- Parametro_a	# parâmetro de discriminação do item 
d <- Parametro_d	#Reparametrização 
g <- Parametro_c  #Parametro de acerto casual 

pars <- c(a,d,g)
#Opções de resposta #nesse caso são 5 alternativas
choices <- subset(Dados[,7:11],colnames=F)

pars <- data.frame(a1=a, d=d,g=g)
mod <- generate.mirt_object(pars, '3PL')
coef(mod,simplify=T)

#Criando um data.frame com as perguntas, alternativas e a resposta correta . 
df <- data.frame(Question=Question,choices,Type = 'radio',Answer=Answer,stringsAsFactors = FALSE)

Candidate <- mirtCAT(df, mod, criteria = 'MI',start_item = 'MI',method='EAP',design_elements = T,theta_range= c(-4,4),quadpts=20,design = list(theta_range= c(-3,3),thetas.start=0))

