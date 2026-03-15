0379 Proyecto Intermodular

# Tercera entrega del proyecto interdisciplinar: Organización de la

# ejecución, la programación y el plan de intervención

# RAs y Criterios implicados

**RA3: Planifica la puesta en funcionamiento o ejecución del proyecto, determinando el
plan de intervención y la documentación asociada**

## Criterios de evaluación:

```
a) Se han secuenciado las actividades ordenándolas en función de las necesidades
de implementación.
b) Se han determinado los recursos y la logística necesaria para cada actividad.
c) Se han identificado las necesidades de permisos y autorizaciones para llevar a
cabo las actividades.
d) Se han determinado los procedimientos de actuación o ejecución de las
actividades.
e) Se han identificado los riesgos inherentes a la ejecución, definiendo el plan de
prevención de riesgos y los medios y equipos necesarios.
f) Se han planificado la asignación de recursos materiales y humanos y los tiempos
de ejecución.
g) Se ha hecho la valoración económica que da respuesta a las condiciones de la
ejecución.
h) Se ha definido y elaborado la documentación necesaria para la ejecución o
ejecución.(FASE IV)
```
# Detalle de la documentación del proyecto

Esta entrega se enfoca en la **Organización de la Ejecución** , transformando el diseño y el
guion de trabajo (Fase I y II) en un plan de acción concreto y ejecutable ( _recuerda que es
una continuación de documento anterior_ ).

En la fase II se comenzó a preparar la organización de la ejecución especificando los
siguientes puntos:

```
 La secuencia de actividades
 La determinación de los recursos necesarios
 Planificación de las actividades del proyecto y asignación de recursos.
```
En la fase III se deba dar comienzo a la ejecución del proyecto diseñado, documentando
los detalles de implementación.

En esta fase se debe detallar:


0379 Proyecto Intermodular

- Detalle de los **requisitos legales y administrativos** necesarios antes de tocar una
    sola tecla. (Criterios RA 3.c)

```
o Licencias : Relación de licencias de software necesarias para la ejecución
(aceptación de términos, licencias open-source, EULA, claves de
producto).
o Permisos de Acceso : Identificación de quién autoriza el acceso a salas de
servidores, armarios de comunicaciones o plataformas cloud.
o Cumplimiento Normativo (RGPD/LOPD) : Si la ejecución implica acceso a
datos reales (migraciones, backups). Se puede diseñar documentos de
confidencialidad o autorización de acceso a datos.
```
- Procedimientos de **ejecución**. Este es el núcleo técnico de la entrega. No se trata
    de decir _qué_ se va a hacer (eso fue la Fase II), sino **cómo** se va a hacer. (Criterio
    3.d).

```
o Implementación de las actividades definidas en el diseño, detallando el
proceso.
o Plan de Pruebas de Aceptación: Procedimiento para verificar que la
ejecución ha sido correcta (Checklist de validación).
o Procedimiento a seguir si la ejecución falla (ej. restauración de snapshot,
desconexión de red).
```
- **Plan de Prevención de Riesgos Laborales** (PRL). Identificación de los riesgos para
    las **personas** durante la ejecución del proyecto, medidas preventivas, medios y
    equipos necesarios. (Criterio 3.e).
- **Valoración económica** de las condiciones de la ejecución (Criterio 3.g).
    Ya en la fase II se realizó un presupuesto. En este caso se debe completar con los
    costes operativos de ejecución (que puede que ya tengas incluidos).

```
o Costes de mano de obra : Cálculo de las horas técnicas requeridas para la
ejecución x coste/hora del perfil técnico.
o Costes de despliegue : si los hubiera, gastos asociados a la puesta en
marcha como pueden ser la contratación de servicios externos,
posiblemente aumento de consumo eléctrico,...).
```
- Documentación para la Ejecución será desarrollada en la siguiente fase de la
    documentación, FASE IV. (Criterio 3.h)


0379 Proyecto Intermodular

# Rúbrica:

```
Criterio (RA3) Sobresaliente (9-10) Notable (7-8) Suficiente (5-6) Insuficiente (0-4)
c) Permisos y
Autorizaciones
```
```
Identifica exhaustivamente
permisos de acceso, licencias y
cumplimiento legal (RGPD)
específico del escenario.
```
```
Identifica los permisos básicos y
licencias, pero con poca
profundidad en la normativa de
datos.
```
```
Menciona permisos
genéricos sin adaptarlos
al proyecto concreto.
```
```
No identifica
permisos ni
autorizaciones.
```
```
d)
Procedimientos
de Ejecución
```
```
Los procedimientos son
detallados, secuenciales y
técnicos (paso a paso),
incluyendo planes de rollback.
```
```
Describe los procedimientos
correctamente, pero falta detalle
técnico o plan de marcha atrás.
```
```
Describe las tareas de
forma genérica sin
explicar el procedimiento
técnico ("cómo se hace").
```
```
No se establecen
procedimientos de
ejecución.
```
```
e) Plan de
Prevención (PRL)
```
```
Análisis completo de riesgos
(físicos, ergonómicos,
psicosociales) adaptados al
puesto de SysAdmin.
```
```
Identifica riesgos básicos
(eléctricos/ergonómicos) y medidas
preventivas correctas.
```
```
Menciona riesgos
genéricos no aplicados a
la informática (ej. cascos
en oficina) o muy escasos.
```
```
No incluye plan de
prevención.
```
```
g) Valoración
Económica
```
```
Calcula con precisión los
costes de mano de obra y
despliegue, diferenciándolos
de la inversión material.
```
```
Valora los costes de ejecución, pero
con cálculos aproximados o
mezclando conceptos de inversión.
```
```
Valoración económica
superficial o errónea.
```
```
No se realiza
valoración
económica de la
ejecución.
```
**Revisa los puntos asociados a los criterios a,b y f del RA 3 desarrollados en el punto anterior por si necesitan corrección ya que se volverán a evaluar.**

```
Criterio de
Evaluación (RA3)
```
```
Sobresaliente (9-10) Notable (7-8) Suficiente (5-6) Insuficiente (0-4)
```

0379 Proyecto Intermodular

```
RA3.a)
Secuenciación de
Actividades
```
```
(Lógica del
proceso)
```
```
La secuenciación es perfecta y lógica.
Se identifican claramente las
dependencias entre tareas (qué
tarea bloquea a cuál). El desglose de
tareas es granular (nivel de sub-
tarea) y no se omiten pasos críticos
(como pruebas o backups previos).
```
```
Las actividades siguen un
orden lógico y coherente. Se
distinguen las fases
principales. El nivel de
detalle es adecuado, aunque
podría haber alguna tarea
menor desordenada o
agrupada en exceso.
```
```
Existe una secuencia
temporal, pero hay
errores de lógica (ej.
configurar un servicio
antes de instalar el S.O.)
o el desglose es
demasiado genérico
(solo fases grandes, sin
tareas).
```
```
La secuenciación es
caótica, ilógica o
inexistente. Faltan pasos
fundamentales para la
ejecución del proyecto.
Simplemente es una lista
de cosas sin orden.
```
```
RA3.b)
Determinación
de Recursos y
Logística
```
```
(Asignación)
```
```
Asignación exhaustiva de recursos
para cada tarea. Se especifica qué
perfil técnico (Rol) realiza la acción y
qué recursos materiales/software
(Hardware, Licencias, Credenciales)
son necesarios en ese momento
exacto.
```
```
Se identifican
correctamente los recursos
materiales y humanos
necesarios por fases o
grupos de tareas. Queda
claro quién hace el trabajo y
con qué herramientas
principales.
```
```
Se mencionan los
recursos de forma
general para todo el
proyecto, sin
vincularlos a tareas
específicas. O se omiten
recursos obvios (como
el tiempo del técnico).
```
```
No se determinan los
recursos necesarios o la
asignación es
errónea/irreal. No se
sabe quién ejecutará las
acciones.
```
```
RA3.f)
Programación
(Diagrama de
Gantt)
```
```
(Visualización
temporal)
```
```
El **Diagrama de Gantt
```

