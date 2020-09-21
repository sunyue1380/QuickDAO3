package cn.schoolwow.quickdao.handler;

import cn.schoolwow.quickdao.QuickDAO;
import cn.schoolwow.quickdao.annotation.*;
import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.Property;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import cn.schoolwow.quickdao.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**实体类信息定义*/
public class DefaultTableDefiner implements TableDefiner{
    private static Logger logger = LoggerFactory.getLogger(DefaultTableDefiner.class);
    /**用于返回QuickDAO对象*/
    private QuickDAO quickDAO;
    /**用于返回QuickDAO对象*/
    private QuickDAOConfig quickDAOConfig;
    /**当前实体类*/
    private Entity entity;

    public DefaultTableDefiner(QuickDAO quickDAO, QuickDAOConfig quickDAOConfig) {
        this.quickDAO = quickDAO;
        this.quickDAOConfig = quickDAOConfig;
    }

    public TableDefiner define(Class clazz) {
        this.entity = quickDAOConfig.entityMap.get(clazz.getName());
        return this;
    }

    @Override
    public TableDefiner tableName(String tableName) {
        entity.tableName = tableName;
        return this;
    }

    @Override
    public TableDefiner comment(String comment) {
        entity.comment = comment;
        return this;
    }

    @Override
    public TablePropertyDefiner property(String fieldName) {
        for(Property property:entity.properties){
            if(property.name.equals(fieldName)){
                return new DefaultTablePropertyDefiner(property,this);
            }
        }
        return null;
    }

    @Override
    public QuickDAO done() {
        return this.quickDAO;
    }

    /**
     * 获取实体类信息
     */
    public synchronized void getEntityMap() throws Exception {
        List<Class> classList = new ArrayList<>();
        //扫描实体类包
        for (String packageName : quickDAOConfig.packageNameMap.keySet()) {
            List<Class> packageClassList = scanEntity(packageName);
            for (Class c : packageClassList) {
                Entity entity = new Entity();
                if(c.getDeclaredAnnotation(TableName.class)!=null){
                    entity.tableName = ((TableName) c.getDeclaredAnnotation(TableName.class)).value();
                }else if ((packageName.length() + c.getSimpleName().length() + 1) == c.getName().length()) {
                    entity.tableName = quickDAOConfig.packageNameMap.get(packageName)+StringUtil.Camel2Underline(c.getSimpleName());
                } else {
                    String prefix = c.getName().substring(packageName.length() + 1, c.getName().lastIndexOf(".")).replace(".", "_");
                    entity.tableName = quickDAOConfig.packageNameMap.get(packageName)+prefix + "@" + StringUtil.Camel2Underline(c.getSimpleName());
                }
                entity.clazz = c;
                quickDAOConfig.entityMap.put(c.getName(), entity);
            }
            classList.addAll(packageClassList);
        }
        //扫描指定实体类
        for(Class c:quickDAOConfig.entityClassMap.keySet()){
            Entity entity = new Entity();
            if(quickDAOConfig.entityClassMap.get(c).isEmpty()){
                entity.tableName = StringUtil.Camel2Underline(c.getSimpleName());
            }else{
                entity.tableName = quickDAOConfig.entityClassMap.get(c)+"@"+StringUtil.Camel2Underline(c.getSimpleName());
            }
            entity.clazz = c;
            quickDAOConfig.entityMap.put(c.getName(), entity);
            classList.add(c);
        }
        for (Class c : classList) {
            Entity entity = quickDAOConfig.entityMap.get(c.getName());
            entity.className = c.getSimpleName();
            if (c.getDeclaredAnnotation(Comment.class) != null) {
                Comment comment = (Comment) c.getDeclaredAnnotation(Comment.class);
                entity.comment = comment.value();
            }
            //属性列表
            List<Property> propertyList = new ArrayList<>();
            //实体包类列表
            List<Field> compositFieldList = new ArrayList<>();
            Field[] fields = getAllField(c,compositFieldList);
            for(Field field:fields){
                Property property = new Property();
                if (null!=field.getAnnotation(ColumnName.class)) {
                    property.column = field.getAnnotation(ColumnName.class).value();
                }else{
                    property.column = StringUtil.Camel2Underline(field.getName());
                }
                if(null!=field.getAnnotation(ColumnType.class)){
                    property.columnType = field.getAnnotation(ColumnType.class).value();
                }
                property.name = field.getName();
                property.simpleTypeName = field.getType().getSimpleName().toLowerCase();
                property.className = field.getType().getName();
                Constraint constraint = field.getDeclaredAnnotation(Constraint.class);
                if(null!=constraint){
                    property.notNull = constraint.notNull();
                    property.unique = constraint.unique();
                    property.check = constraint.check();
                    property.defaultValue = constraint.defaultValue();
                    property.unionUnique = constraint.unionUnique();
                }
                if(property.name.equals("id")){
                    property.id = true;
                    property.strategy = IdStrategy.AutoIncrement;
                }
                Id id = field.getDeclaredAnnotation(Id.class);
                if(null!=id){
                    property.id = true;
                    property.strategy = id.strategy();
                }
                TableField tableField = field.getDeclaredAnnotation(TableField.class);
                if(null!=tableField){
                    property.createdAt = tableField.createdAt();
                    property.updateAt = tableField.updatedAt();
                }
                property.index = field.getDeclaredAnnotation(Index.class) != null;
                if(null!=field.getDeclaredAnnotation(Comment.class)){
                    property.comment = field.getDeclaredAnnotation(Comment.class).value();
                }
                property.foreignKey = field.getDeclaredAnnotation(ForeignKey.class);
                property.entity = entity;
                propertyList.add(property);
            }
            entity.properties = propertyList.toArray(new Property[0]);
            if (compositFieldList.size() > 0) {
                entity.compositFields = compositFieldList.toArray(new Field[0]);
            }
        }

        logger.debug("[获取实体信息]实体类个数:{}", quickDAOConfig.entityMap.size());
    }

    /**处理实体类相关约束*/
    public synchronized void handleEntityMap(){
        for(Entity entity: quickDAOConfig.entityMap.values()){
            List<Property> indexPropertyList = new ArrayList<>();
            List<Property> uniquePropertyList = new ArrayList<>();
            List<Property> checkPropertyList = new ArrayList<>();
            List<Property> foreignKeyPropertyList = new ArrayList<>();
            for(Property property : entity.properties){
                if(property.id){
                    entity.id = property;
                    property.notNull = true;
                    property.unique = true;
                    property.comment = "自增id";
                    //@Id注解生成策略为默认值又在全局指定里Id生成策略则使用全局策略
                    if(property.strategy==IdStrategy.AutoIncrement&&null!=quickDAOConfig.idStrategy){
                        property.strategy = quickDAOConfig.idStrategy;
                    }
                }
                if(property.unique){
                    property.notNull = true;
                    if(!property.id){
                        property.index = true;
                    }
                }
                if(property.index){
                    indexPropertyList.add(property);
                }
                if(property.unique&&property.unionUnique){
                    uniquePropertyList.add(property);
                }
                if(null!=property.check&&!property.check.isEmpty()){
                    checkPropertyList.add(property);
                }
                if(null!=property.foreignKey){
                    foreignKeyPropertyList.add(property);
                }
            }
            entity.indexProperties = indexPropertyList.toArray(new Property[0]);
            entity.uniqueKeyProperties = uniquePropertyList.toArray(new Property[0]);
            entity.checkProperties = checkPropertyList.toArray(new Property[0]);
            entity.foreignKeyProperties = foreignKeyPropertyList.toArray(new Property[0]);
        }
    };

    /**
     * 扫描实体包
     */
    private List<Class> scanEntity(String packageName) throws ClassNotFoundException, IOException {
        String packageNamePath = packageName.replace(".", "/");
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL url = classLoader.getResource(packageNamePath);
        if (url == null) {
            logger.warn("[实体类路径不存在]{}",packageNamePath);
            return new ArrayList<>();
        }
        final List<Class> classList = new ArrayList<>();
        switch (url.getProtocol()) {
            case "file": {
                File file = new File(url.getFile());
                //TODO 对于有空格或者中文路径会无法识别
                logger.info("[类文件路径]{}", file.getAbsolutePath());
                if (!file.isDirectory()) {
                    throw new IllegalArgumentException("包名不是合法的文件夹!" + url.getFile());
                }
                String indexOfString = packageName.replace(".", "/");
                Files.walkFileTree(file.toPath(),new SimpleFileVisitor<Path>(){
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                            throws IOException
                    {
                        File f = file.toFile();
                        if(f.getName().endsWith(".class")){
                            String path = f.getAbsolutePath().replace("\\", "/");
                            int startIndex = path.indexOf(indexOfString);
                            String className = path.substring(startIndex, path.length() - 6).replace("/", ".");
                            try {
                                classList.add(Class.forName(className));
                            } catch (ClassNotFoundException e) {
                                logger.warn("[实体类不存在]{}",className);
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
            break;
            case "jar": {
                JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
                if (null != jarURLConnection) {
                    JarFile jarFile = jarURLConnection.getJarFile();
                    if (null != jarFile) {
                        Enumeration<JarEntry> jarEntries = jarFile.entries();
                        while (jarEntries.hasMoreElements()) {
                            JarEntry jarEntry = jarEntries.nextElement();
                            String jarEntryName = jarEntry.getName();
                            if (jarEntryName.contains(packageNamePath) && jarEntryName.endsWith(".class")) { //是否是类,是类进行加载
                                String className = jarEntryName.substring(0, jarEntryName.lastIndexOf(".")).replaceAll("/", ".");
                                classList.add(classLoader.loadClass(className));
                            }
                        }
                    }
                }
            }
            break;
        }
        if (classList.size() == 0) {
            logger.warn("[扫描实体类信息为空]前缀:{},包名:{}", quickDAOConfig.packageNameMap.get(packageName), packageName);
            return classList;
        }
        Stream<Class> stream = classList.stream().filter((clazz) -> {
            return !needIgnoreClass(clazz);
        });
        return stream.collect(Collectors.toList());
    }

    /**
     * 获得该类所有字段(包括父类字段)
     * @param clazz 类
     * */
    private Field[] getAllField(Class clazz,List<Field> compositFieldList){
        List<Field> fieldList = new ArrayList<>();
        Class tempClass = clazz;
        while (null != tempClass) {
            Field[] fields = tempClass.getDeclaredFields();
            Field.setAccessible(fields, true);
            for (Field field : fields) {
                if(Modifier.isStatic(field.getModifiers())||Modifier.isFinal(field.getModifiers())|| Modifier.isTransient(field.getModifiers())){
                    logger.debug("[跳过常量或静态变量]{},该属性被static或者final修饰!", field.getName());
                    continue;
                }
                if (field.getDeclaredAnnotation(Ignore.class) != null) {
                    logger.debug("[跳过实体属性]{},该属性被Ignore注解修饰!", field.getName());
                    continue;
                }
                //跳过List类型和数组类型
                if(field.getType().isArray()||(!field.getType().isPrimitive()&&isCollection(field.getType()))){
                    continue;
                }
                if(needIgnoreClass(field.getType())){
                    continue;
                }
                //跳过实体包类
                if (isCompositProperty(field.getType())) {
                    compositFieldList.add(field);
                    continue;
                }
                field.setAccessible(true);
                fieldList.add(field);
            }
            tempClass = tempClass.getSuperclass();
            if (null!=tempClass&&"java.lang.Object".equals(tempClass.getName())) {
                break;
            }
        }
        return fieldList.toArray(new Field[0]);
    }

    /**
     * 判断是否是实体包类
     **/
    private boolean isCompositProperty(Class clazz) {
        Set<String> packageNameSet = quickDAOConfig.packageNameMap.keySet();
        for (String packageName : packageNameSet) {
            if (clazz.getName().contains(packageName)) {
                return true;
            }
        }
        Set<Class> classSet = quickDAOConfig.entityClassMap.keySet();
        for (Class c : classSet) {
            if(c.getName().equals(clazz.getName())){
                return true;
            }
        }
        return false;
    }

    /**是否集合*/
    private boolean isCollection(Class _class){
        Stack<Class[]> stack = new Stack<>();
        stack.push(_class.getInterfaces());
        while(!stack.isEmpty()){
            Class[] classes = stack.pop();
            for(Class clazz:classes){
                if(clazz.getName().equals(Collection.class.getName())){
                    return true;
                }
                Class[] subClasses = clazz.getInterfaces();
                if(null!=subClasses&&subClasses.length>0){
                    stack.push(subClasses);
                }
            }
        }
        return false;
    }

    /**是否需要忽略该类*/
    private boolean needIgnoreClass(Class clazz){
        if(clazz.isEnum()){
            return true;
        }
        if (clazz.getAnnotation(Ignore.class) != null) {
            return true;
        }
        //根据类过滤
        if(null!=quickDAOConfig.ignoreClassList){
            for(Class _clazz:quickDAOConfig.ignoreClassList){
                if(_clazz.getName().equals(clazz.getName())){
                    return true;
                }
            }
        }
        //根据包名过滤
        if (null!=quickDAOConfig.ignorePackageNameList) {
            for (String ignorePackageName : quickDAOConfig.ignorePackageNameList) {
                if (clazz.getName().contains(ignorePackageName)) {
                    return true;
                }
            }
        }

        if(null!=quickDAOConfig.predicate){
            if(quickDAOConfig.predicate.test(clazz)){
                return true;
            }
        }
        return false;
    }
}
