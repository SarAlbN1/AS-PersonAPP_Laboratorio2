INSERT INTO 
	`persona_db`.`persona`(`cc`,`nombre`,`apellido`,`genero`,`edad`) 
VALUES
	(123456789,'Pepe','Perez','M',30),
	(987654321,'Pepito','Perez','M',null),
	(321654987,'Pepa','Juarez','F',30),
	(147258369,'Pepita','Juarez','F',10),
	(963852741,'Fede','Perez','M',18);

INSERT INTO
	`persona_db`.`profesion`(`id`,`nom`,`des`)
VALUES
	(1,'Ingeniero de Sistemas','Diseño y construcción de software'),
	(2,'Medico','Atención en salud');

INSERT INTO
	`persona_db`.`telefono`(`num`,`oper`,`duenio`)
VALUES
	('3001234567','Claro',123456789),
	('3107654321','Movistar',123456789),
	('3209876543','Tigo',321654987);

INSERT INTO
	`persona_db`.`estudios`(`id_prof`,`cc_per`,`fecha`,`univer`)
VALUES
	(1,123456789,'2015-12-01','Pontificia Universidad Javeriana'),
	(2,321654987,'2018-06-15','Universidad Nacional');
