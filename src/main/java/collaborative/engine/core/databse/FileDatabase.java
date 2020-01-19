package collaborative.engine.core.databse;

import collaborative.engine.core.Collaboratory;
import collaborative.engine.core.ContentSystem;
import collaborative.engine.core.identify.Identifier;
import collaborative.engine.core.identify.ObjectId;
import pact.support.FileSupport;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import static collaborative.engine.core.databse.StandardRemoveOption.IMMEDIATE;

/**
 * 本地文件数据库，提供一致添加、删除等文件操作。并且实现文件的并发访问与读写。
 *
 * @author XyParaCrim
 */
@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class FileDatabase implements Database {

    private final File directory;

    private final Identifier identifier;

    private final Collaboratory collaboratory;

    private final ContentSystem contentSystem;

    public FileDatabase(Collaboratory collaboratory, File directory, ContentSystem contentSystem) {
        this.collaboratory = collaboratory;
        this.directory = directory;
        this.contentSystem = contentSystem;
        this.identifier = contentSystem.newIdentifier();
    }

    @Override
    public boolean contains(ObjectId objectId) {
        if (identifier.contains(objectId)) {
            return true;
        }
        if (objectId.location().exists()) {
            identifier.add(objectId);
            return true;
        }
        return false;
    }

    @Override
    public InsertResult insert(File file, Set<InsertOption> options) {
        if (options.contains(PromissoryInsertOption.TEMP)) {
            return insertTemp(file, options);
        }

        return InsertResult.unsupported();
    }

    private InsertResult insertTemp(File temp, Collection<InsertOption> options) {
        // 插入的为临时文件，并且信任这次插入操作
        // 这一步是因为，可能这些objectId可能实际就是在文件系统里的文件
        ObjectId objectId;
        while (contains(objectId = identifier.newId())) {
            objectId.cache();
        }

        try {
            FileSupport.mkdir(objectId.groupLocation());
            FileSupport.atomicMove(temp, objectId.location());

            // 这一步表识该id已使用，但很可能是不对的，再议
            identifier.add(objectId);
        } catch (IOException e) {
            return InsertResult.failure(objectId, e);
        }

        return InsertResult.inserted(objectId.unmodifiable());
    }

    @Override
    public RemoveResult remove(ObjectId objectId, Set<RemoveOption> options) {
        for (; ; ) {
            if (!contains(objectId)) {
                return RemoveResult.absent(objectId);
            }

            if (objectId.acquire()) {
                break;
            }

            // 如果需要立即删除，但此时的资源在忙碌中，则返回退出返回忙碌状态
            if (options.contains(IMMEDIATE)) {
                return RemoveResult.busy(objectId);
            }
        }

        // 到达这一步表示已经获得了它的独占权
        // tip：其他的地方同样可以解除这个独占权，所以约定按照一定的方式获取其权利
        if (!contains(objectId)) {
            return RemoveResult.likeRemoved(objectId);
        }

        // 此时独占后且文件系统存在这个文件，执行删除
        try {
            FileSupport.deleteFile(objectId.location());
        } catch (IOException e) {
            return RemoveResult.failure(objectId, e);
        } finally {
            objectId.uncache();
            objectId.release();
        }

        return RemoveResult.removed(objectId);
    }

    @Override
    public File location() {
        return directory;
    }

    @Override
    public Identifier identifier() {
        return identifier;
    }
}
