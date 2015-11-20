
#Informar quais foram as primeiras perguntas respondidas e se estão certas ou erradas 
Candidate <- updateDesign(Candidate, items = c(x,x,x), responses = c(0,0,0))

#Determinando Theta inicial 
Candidate$person$Update.thetas(Candidate$design, Candidate$test)
Teta_Estimado <- Candidate$person$thetas